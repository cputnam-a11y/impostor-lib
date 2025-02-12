package dev.shadowsoffire.placebo.reload;

import com.mojang.datafixers.util.Either;
import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.reload.DynamicRegistry.SyncManagement;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public class ReloadListenerPayloads {

    public static record Start(String path) implements CustomPacketPayload {

        public static final Runnable registerClient = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
                                                      ? Start.ClientHandler::register
                                                      : () -> {
                                                      };

        public static final Type<Start> TYPE = new Type<>(Placebo.loc("reload_sync_start"));

        public static final StreamCodec<FriendlyByteBuf, Start> CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, Start::path,
                Start::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void init() {
            PayloadTypeRegistry.playS2C().register(TYPE, CODEC);
            registerClient.run();
        }

        private static class ClientHandler implements ClientPlayNetworking.PlayPayloadHandler<Start> {
            @Override
            public void receive(Start payload, ClientPlayNetworking.Context context) {
                SyncManagement.initSync(payload.path);
            }

            public static void register() {
                ClientPlayNetworking.registerGlobalReceiver(TYPE, new ClientHandler());
            }
        }
    }

    public record Content<V extends CodecProvider<? super V>>(String path, ResourceLocation key, Either<V, ByteBuf> item) implements CustomPacketPayload {

        public static final Runnable registerClient = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
                                                      ? Content.ClientHandler::register
                                                      : () -> {
                                                      };

        public static final Type<Content<?>> TYPE = new Type<>(Placebo.loc("reload_sync_content"));

        public static final StreamCodec<RegistryFriendlyByteBuf, Content<?>> CODEC = StreamCodec.of(Content::write, Content::read);

        public Content(String path, ResourceLocation key, V item) {
            this(path, key, Either.left(item));
        }

        public Content(String path, ResourceLocation key, ByteBuf buf) {
            this(path, key, Either.right(buf));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void init() {
            PayloadTypeRegistry.playS2C().register(TYPE, CODEC);
            registerClient.run();
        }

        @SuppressWarnings("unchecked")
        public static <V extends CodecProvider<? super V>> void write(RegistryFriendlyByteBuf buf, Content<?> payload) {
            buf.writeUtf(payload.path, 50);
            buf.writeResourceLocation(payload.key);
            SyncManagement.writeItem(payload.path, (V) payload.item.orThrow(), buf);
        }

        /**
         * Reads a content payload. We defer deserialization of the underlying object, since it may depend on the state of
         * other registries that are being setup on the main thread.
         */
        public static <V extends CodecProvider<? super V>> Content<V> read(RegistryFriendlyByteBuf buf) {
            String path = buf.readUtf(50);
            ResourceLocation key = buf.readResourceLocation();

            int size = buf.writerIndex() - buf.readerIndex();
            ByteBuf itemBuf = Unpooled.buffer(size, size);
            buf.readBytes(itemBuf);
            return new Content<V>(path, key, itemBuf);
        }

        private static class ClientHandler implements ClientPlayNetworking.PlayPayloadHandler<Content<?>> {
            @Override
            public void receive(Content<?> payload, ClientPlayNetworking.Context context) {
                RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(payload.item.right().get(), context.player().registryAccess());

                try {
                    Object value = SyncManagement.readItem(payload.path, buf);
                    SyncManagement.acceptItem(payload.path, payload.key, value);
                } catch (Exception ex) {
                    Placebo.LOGGER.error("Failure when deserializing a dynamic registry object via network: Registry: {}, Object ID: {}", payload.path, payload.key);
                    throw ex;
                }
            }

            public static void register() {
                ClientPlayNetworking.registerGlobalReceiver(TYPE, new ClientHandler());
            }
        }
    }

    public record End(String path) implements CustomPacketPayload {

        public static final Runnable registerClient = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
                                                      ? ClientHandler::register
                                                      : () -> {
                                                      };

        public static final Type<End> TYPE = new Type<>(Placebo.loc("reload_sync_end"));

        public static final StreamCodec<FriendlyByteBuf, End> CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8,
                End::path,
                End::new
        );

        @Override
        @NotNull
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void init() {
            PayloadTypeRegistry.playS2C().register(TYPE, CODEC);
            registerClient.run();
        }

        private static class ClientHandler implements ClientPlayNetworking.PlayPayloadHandler<End> {

            public static void register() {
                ClientPlayNetworking.registerGlobalReceiver(TYPE, new ClientHandler());
            }

            @Override
            public void receive(End payload, ClientPlayNetworking.Context context) {
                SyncManagement.endSync(payload.path);
            }
        }
    }
}

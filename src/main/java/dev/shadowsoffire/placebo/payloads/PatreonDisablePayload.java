package dev.shadowsoffire.placebo.payloads;

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.patreon.TrailsManager;
import dev.shadowsoffire.placebo.patreon.WingsManager;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.ByIdMap;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.function.IntFunction;

public record PatreonDisablePayload(CosmeticType cosmetic, UUID id) implements CustomPacketPayload {

    public static final Type<PatreonDisablePayload> TYPE = new Type<>(Placebo.loc("patreon_disable"));

    public static final StreamCodec<FriendlyByteBuf, PatreonDisablePayload> CODEC = StreamCodec.composite(
            CosmeticType.STREAM_CODEC, PatreonDisablePayload::cosmetic,
            UUIDUtil.STREAM_CODEC, PatreonDisablePayload::id,
            PatreonDisablePayload::new);

    public static final Runnable registerClient = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
                                                  ? ClientHandler::register
                                                  : () -> {
                                                  };

    @Override
    @NotNull
    public Type<PatreonDisablePayload> type() {
        return TYPE;
    }

    public enum CosmeticType {
        TRAILS,
        WINGS;

        public static final IntFunction<CosmeticType> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final StreamCodec<ByteBuf, CosmeticType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
    }

    static {
        PayloadTypeRegistry.playC2S().register(TYPE, CODEC);
        PayloadTypeRegistry.playS2C().register(TYPE, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) -> {
            context.server().getPlayerList().getPlayers()
                    .forEach(player -> {
                        ServerPlayNetworking.send(
                                player,
                                new PatreonDisablePayload(
                                        payload.cosmetic(),
                                        context.player().getUUID()
                                )
                        );
                    });
        });
        registerClient.run();
    }

    public static void init() {

    }

    public static class ClientHandler implements ClientPlayNetworking.PlayPayloadHandler<PatreonDisablePayload> {
        @Override
        public void receive(PatreonDisablePayload payload, ClientPlayNetworking.Context context) {
            Set<UUID> set = switch (payload.cosmetic()) {
                case TRAILS -> TrailsManager.DISABLED;
                case WINGS -> WingsManager.DISABLED;
            };

            if (set.contains(payload.id)) {
                set.remove(payload.id);
            } else set.add(payload.id);
        }

        public static void register() {
            ClientPlayNetworking.registerGlobalReceiver(TYPE, new ClientHandler());
        }
    }
}

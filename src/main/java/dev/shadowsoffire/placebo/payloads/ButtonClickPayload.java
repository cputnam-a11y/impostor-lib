package dev.shadowsoffire.placebo.payloads;

import dev.shadowsoffire.placebo.Placebo;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Allows for easy implementations of client->server button presses. Sends an integer that allows for arbitrary data encoding schemes within the integer
 * space.<br>
 * The Container must implement {@link IButtonContainer}.<br>
 * Defer to using using {@link MultiPlayerGameMode#handleInventoryButtonClick} and {@link AbstractContainerMenu#clickMenuButton} when the buttonId can be a
 * byte.
 */
public record ButtonClickPayload(int button) implements CustomPacketPayload {

    public static final Type<ButtonClickPayload> TYPE = new Type<>(Placebo.loc("button_click"));

    public static final StreamCodec<FriendlyByteBuf, ButtonClickPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ButtonClickPayload::button,
            ButtonClickPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public interface IButtonContainer {
        void onButtonClick(int id);
    }

    static {
        ServerPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) -> {
            if (context.player().containerMenu instanceof IButtonContainer buttonContainer) {
                buttonContainer.onButtonClick(payload.button());
            }
        });
        PayloadTypeRegistry.playC2S().register(TYPE, CODEC);
    }

    public static void init() {}
}

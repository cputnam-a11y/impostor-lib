package io.github.cputnama11y.patch.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface IPayloadContext {
    BackingContext getBackingContext();

    interface BackingContext {
        default boolean isClient() {
            return false;
        }

        default boolean isServer() {
            return false;
        }
    }

    record ClientBackingContext(ClientPlayNetworking.Context context) implements BackingContext {
        @Override
        public boolean isClient() {
            return true;
        }
    }

    record ServerBackingContext(ServerPlayNetworking.Context context) implements BackingContext {

        @Override
        public boolean isServer() {
            return true;
        }
    }
}
package io.github.cputnama11y.patch.hook;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class ServerCaptureNeoForgeDoesItDontKillMe {
    private static MinecraftServer server;
    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(s -> server = s);
        ServerLifecycleEvents.SERVER_STOPPING.register(s -> server = null);
    }
    public static MinecraftServer get() {
        return server;
    }
}

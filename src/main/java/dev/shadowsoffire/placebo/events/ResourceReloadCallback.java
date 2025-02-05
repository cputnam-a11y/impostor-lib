package dev.shadowsoffire.placebo.events;

import dev.shadowsoffire.placebo.config.Configuration;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * This event is fired whenever client or server resources are reloaded.
 * It can be used to subscribe to both types without needing register sided handlers, which is useful for reloading {@link Configuration} files.
 * <p>
 * Consumers of this event should not rely on its timing with respect to other {@link PreparableReloadListener}s.
 */
public interface ResourceReloadCallback {
    public static final Event<ResourceReloadCallback> EVENT = EventFactory.createArrayBacked(
            ResourceReloadCallback.class,
            listeners -> (resourceManager, isClient) -> {
                for (ResourceReloadCallback listener : listeners) {
                    listener.onResourceReload(resourceManager, isClient);
                }
            });

    void onResourceReload(ResourceManager resourceManager, boolean isClient);
//    public ResourceManager getResourceManager() {
//        return this.resourceManager;
//    }
//
//    public LogicalSide getSide() {
//        return this.side;
//    }

}

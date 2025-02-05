package dev.shadowsoffire.placebo.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The AnvilFallEvent is fired when a falling anvil lands on a block.
 */
public interface AnvilLandCallback {
    Event<AnvilLandCallback> EVENT = EventFactory.createArrayBacked(
            AnvilLandCallback.class,
            listeners ->
                    (level, pos, newState, oldState, entity) -> {
                        for (AnvilLandCallback listener : listeners) {
                            listener.onAnvilLand(level, pos, newState, oldState, entity);
                        }
                    });

    void onAnvilLand(Level level, BlockPos pos, BlockState newState, BlockState oldState, FallingBlockEntity entity);
}

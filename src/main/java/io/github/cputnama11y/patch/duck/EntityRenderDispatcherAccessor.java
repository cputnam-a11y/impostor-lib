package io.github.cputnama11y.patch.duck;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.player.Player;

import java.util.Set;

public interface EntityRenderDispatcherAccessor {
    public Set<PlayerSkin.Model> placebo$getModels();
    EntityRenderer<? extends Player> placebo$getSkin(PlayerSkin.Model model);
}

package io.github.cputnama11y.patch.mixin;

import io.github.cputnama11y.patch.duck.EntityRenderDispatcherAccessor;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.Set;

import static net.minecraft.client.resources.PlayerSkin.Model;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin implements EntityRenderDispatcherAccessor {
    @Shadow
    private Map<Model, EntityRenderer<? extends Player>> playerRenderers;

    @Override
    public Set<Model> placebo$getModels() {
        return this.playerRenderers.keySet();
    }

    @Override
    public EntityRenderer<? extends Player> placebo$getSkin(Model model) {
        return this.playerRenderers.get(model);
    }
}

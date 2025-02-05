package dev.shadowsoffire.placebo.mixin.client;

import dev.shadowsoffire.placebo.util.DrawsOnLeft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = AbstractContainerScreen.class, remap = false)
public abstract class AbstractContainerScreenMixin extends Screen implements DrawsOnLeft {
    @Shadow
    protected int leftPos;

    protected AbstractContainerScreenMixin(Component component) {
        super(component);
    }

    @Override
    public int placebo$getGuiLeft() {
        return this.leftPos;
    }
    @Override
    public Font placebo$getFont() {
        return this.font;
    }
}

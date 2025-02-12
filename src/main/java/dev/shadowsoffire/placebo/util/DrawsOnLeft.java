package dev.shadowsoffire.placebo.util;

import dev.shadowsoffire.placebo.mixin.client.AbstractContainerScreenMixin;
import io.github.cputnama11y.patch.mixin.GUIGraphicsAccessor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;

/**
 * Implement this on a screen class to be able to call {@link #drawOnLeft(GuiGraphics, List, int)}
 * <p>
 * Applied to all screens via {@link AbstractContainerScreenMixin}.
 */
public interface DrawsOnLeft {
    int placebo$getGuiLeft();
    Font placebo$getFont();
    /**
     * Renders a list of text as a tooltip attached to the left edge of the currently open container screen.
     * <p>
     * This method will automatically compress the text to fit in the available space between the left edge of the screen and the left edge of the game window.
     */
    default void drawOnLeft(GuiGraphics gfx, List<? extends FormattedText> list, int y) {
        if (list.isEmpty()) return;
        int xPos = placebo$getGuiLeft() - 16 - list.stream().map(placebo$getFont()::width).max(Integer::compare).get();
        int maxWidth = 9999;
        if (xPos < 0) {
            maxWidth = placebo$getGuiLeft() - 6;
            xPos = -8;
        }

        List<FormattedText> split = new ArrayList<>();
        int _maxWidth = maxWidth;
        list.forEach(text -> {
            Style style = text instanceof Component comp ? comp.getStyle() : Style.EMPTY;
            placebo$getFont().getSplitter().splitLines(text, _maxWidth, style, (splitLine, isBlank) -> split.add(splitLine));
        });
        List<ClientTooltipComponent> tooltips = split.stream()
                .map(Language.getInstance()::getVisualOrder)
                .map(ClientTooltipComponent::create)
                .toList();
        ((GUIGraphicsAccessor)gfx).placebo$renderTooltipInternal(
                placebo$getFont(),
                tooltips,
                xPos,
                y,
                DefaultTooltipPositioner.INSTANCE
        );
//        gfx.renderComponentTooltip(placebo$getFont(), split, xPos, y, ItemStack.EMPTY);
    }

    /**
     * Renders a list of text as a tooltip attached to the left edge of the currently open container screen.
     * <p>
     * This method will compress the text to fit in the specified maxWidth, ignoring the size of the game window.
     */
    default void drawOnLeft(GuiGraphics gfx, List<? extends FormattedText> list, int y, int maxWidth) {
        if (list.isEmpty()) {
            return;
        }

        List<FormattedText> split = new ArrayList<>();
        list.forEach(text -> {
            Style style = text instanceof Component comp ? comp.getStyle() : Style.EMPTY;
            placebo$getFont().getSplitter().splitLines(text, maxWidth, style, (splitLine, isBlank) -> split.add(splitLine));
        });
        List<ClientTooltipComponent> tooltips = split.stream()
                .map(Language.getInstance()::getVisualOrder)
                        .map(ClientTooltipComponent::create)
                                .toList();
        int xPos = placebo$getGuiLeft() - 16 - split.stream().map(placebo$getFont()::width).max(Integer::compare).get();
        ((GUIGraphicsAccessor)gfx).placebo$renderTooltipInternal(
                placebo$getFont(),
                tooltips,
                xPos,
                y,
                DefaultTooltipPositioner.INSTANCE
        );
//        gfx.renderComponentTooltip(placebo$getFont(), split, xPos, y);
    }

    default AbstractContainerScreen<?> __ths() {
        return (AbstractContainerScreen<?>) this;
    }

    static void draw(AbstractContainerScreen<?> screen, GuiGraphics gfx, List<Component> list, int y) {
        ((DrawsOnLeft) screen).drawOnLeft(gfx, list, y);
    }

}

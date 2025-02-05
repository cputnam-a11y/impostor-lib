package io.github.cputnama11y.patch.mixin;

import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(TextColor.class)
public interface TextColorAccessor {
    @Accessor("NAMED_COLORS")
    static Map<String, TextColor> placebo$getNamedColors() {
        throw new AssertionError("Mixin failed to apply");
    }
    @Accessor("NAMED_COLORS")
    @Mutable
    static void placebo$setNamedColors(Map<String, TextColor> namedColors) {
        throw new AssertionError("Mixin failed to apply");
    }
    @Accessor("name")
    String placebo$getName();
}

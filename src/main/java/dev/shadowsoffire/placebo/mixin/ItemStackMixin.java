package dev.shadowsoffire.placebo.mixin;

import dev.shadowsoffire.placebo.util.CachedObject;
import dev.shadowsoffire.placebo.util.CachedObject.CachedObjectSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.ToIntFunction;

@Mixin(ItemStack.class)
public class ItemStackMixin implements CachedObjectSource {
    @Unique
    private volatile Map<ResourceLocation, CachedObject<?>> cachedObjects = null;

    @Unique
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOrCreate(ResourceLocation id, Function<ItemStack, T> deserializer, ToIntFunction<ItemStack> hasher) {
        var cachedObj = this.getOrCreate().computeIfAbsent(id, key -> new CachedObject<>(key, deserializer, hasher));
        return (T) cachedObj.get((ItemStack) (Object) this);
    }

    @Unique
    private Map<ResourceLocation, CachedObject<?>> getOrCreate() {
        if (this.cachedObjects == null) {
            synchronized (this) {
                if (this.cachedObjects == null) this.cachedObjects = new ConcurrentHashMap<>();
            }
        }
        return this.cachedObjects;
    }

}

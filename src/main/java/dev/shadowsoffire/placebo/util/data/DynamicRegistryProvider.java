package dev.shadowsoffire.placebo.util.data;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import dev.shadowsoffire.placebo.reload.DynamicRegistry.DataGenPopulator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Data provider for objects registered to a {@link DynamicRegistry}.
 */
public abstract class DynamicRegistryProvider<R extends CodecProvider<R>> implements DataProvider {

    protected final CompletableFuture<HolderLookup.Provider> lookupProvider;
    protected final PackOutput.PathProvider pathProvider;
    protected final DynamicRegistry<R> registry;
    protected final List<CompletableFuture<?>> futures = new ArrayList<>();

    private CachedOutput cachedOutput;
    private DataGenPopulator<R> populator;

    /**
     * Creates a new provider. Subclasses should create a public constructor that inlines the registry parameter.
     *
     * @param output     The pack output. The final output folder will be for a data pack using the registry path.
     * @param registries The registry lookup for this datagen instance.
     * @param registry   The registry for which objects are being generated for
     */
    public DynamicRegistryProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries, DynamicRegistry<R> registry) {
        this.lookupProvider = registries;
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, registry.getPath());
        this.registry = registry;
    }

//    /**
//     * @deprecated Use {@link #DynamicRegistryProvider(PackOutput, CompletableFuture, DynamicRegistry)}
//     */
//    @Deprecated(forRemoval = true)
//    public DynamicRegistryProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries, DynamicRegistry<R> registry) {
//        this.lookupProvider = registries;
//        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, registry.getPath());
//        this.registry = registry;
//    }

    @Override
    public final CompletableFuture<?> run(CachedOutput pOutput) {
        this.cachedOutput = pOutput;
        DataGenPopulator.runScoped(registry, populator -> {
            this.populator = populator;
            this.generate();
            this.populator = null;
        });
        return CompletableFuture.allOf(this.futures.toArray(CompletableFuture[]::new));
    }

    /**
     * Adds an individual object to this provider.
     *
     * @param id     The id of the object
     * @param object The object
     */
    protected final void add(ResourceLocation id, R object) {
        this.populator.register(id, object);
        this.futures.add(this.lookupProvider.thenCompose(regs -> {
            DynamicOps<JsonElement> ops = regs.createSerializationContext(JsonOps.INSTANCE);
            return DataProvider.saveStable(this.cachedOutput, this.registry.elementCodec().encodeStart(ops, object).getOrThrow(), this.pathProvider.json(id));
        }));
    }

    /**
     * Adds an individual object to this provider with a specified list of conditions.
     *
     * @param id         The id of the object
     * @param object     The object
     * @param conditions Conditions required for the object to load.
     */
    @SuppressWarnings("unchecked")
    protected final void addConditionally(ResourceLocation id, R object, ResourceCondition... conditions) {
        this.populator.register(id, object);
        this.futures.add(this.lookupProvider.thenCompose(regs -> {
            DynamicOps<JsonElement> ops = regs.createSerializationContext(JsonOps.INSTANCE);
            var res = ResourceCondition.LIST_CODEC.encodeStart(ops, List.of(conditions));
            return DataProvider.saveStable(this.cachedOutput, res.flatMap(prefix -> ((Codec<R>) object.getCodec()).encode(object, ops, prefix)).getOrThrow(), this.pathProvider.json(id));
        }));
    }

    /**
     * Generates all items provided by this provider.
     * <p>
     * Use {@link #add(ResourceLocation, CodecProvider)} to supply items.
     */
    public abstract void generate();

}

package dev.shadowsoffire.placebo;

import com.mojang.brigadier.CommandDispatcher;
import dev.shadowsoffire.placebo.color.GradientColor;
import dev.shadowsoffire.placebo.commands.PlaceboCommand;
import dev.shadowsoffire.placebo.events.ResourceReloadCallback;
import dev.shadowsoffire.placebo.loot.StackLootEntry;
import dev.shadowsoffire.placebo.network.PayloadHelper;
import dev.shadowsoffire.placebo.payloads.ButtonClickPayload;
import dev.shadowsoffire.placebo.payloads.PatreonDisablePayload;
import dev.shadowsoffire.placebo.reload.ReloadListenerPayloads;
import dev.shadowsoffire.placebo.systems.gear.GearSetRegistry;
import dev.shadowsoffire.placebo.systems.mixes.MixRegistry;
import dev.shadowsoffire.placebo.systems.wanderer.WandererTradesRegistry;
import dev.shadowsoffire.placebo.tabs.TabFillingRegistry;
import dev.shadowsoffire.placebo.util.PlaceboUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

//@Mod(Placebo.MODID)
public class Placebo implements ModInitializer {

    public static final String MODID = "placebo";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public Placebo() {
        bus.register(new PayloadHelper());
    }

    public void setup() {
        PayloadHelper.registerPayload(new ButtonClickPayload.Provider());
        PayloadHelper.registerPayload(new PatreonDisablePayload.Provider());
        PayloadHelper.registerPayload(new ReloadListenerPayloads.Start.Provider());
        PayloadHelper.registerPayload(new ReloadListenerPayloads.Content.Provider<>());
        PayloadHelper.registerPayload(new ReloadListenerPayloads.End.Provider());
        PlaceboUtil.registerCustomColor(GradientColor.RAINBOW);
        GearSetRegistry.INSTANCE.registerToBus();
        WandererTradesRegistry.INSTANCE.registerToBus();
        MixRegistry.INSTANCE.registerToBus();
    }

    //    @SubscribeEvent
    public void register() {
        Registry.register(
                BuiltInRegistries.LOOT_POOL_ENTRY_TYPE,
                loc("stack_entry"),
                StackLootEntry.TYPE
        );
//        e.register(Registries.LOOT_POOL_ENTRY_TYPE, helper -> {
//            helper.register(loc("stack_entry"), StackLootEntry.TYPE);
//        });
    }

    public void registerCommands(CommandDispatcher<CommandSourceStack> pDispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        PlaceboCommand.register(pDispatcher, registryAccess);
    }

    public void registerServerReloadListener() {
        ResourceManagerHelper
                .get(PackType.SERVER_DATA)
                .registerReloadListener(
                        new IdentifiableResourceReloadListener() {
                            final ResourceLocation id = loc("placebo_event");
                            @Override
                            public ResourceLocation getFabricId() {
                                return id
                            }

                            @Override
                            public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) {
                                return CompletableFuture.runAsync(
                                        () -> ResourceReloadCallback.EVENT.invoker().onResourceReload(resourceManager, false)
                                );
                            }
                        }
                );
    }

//    public void serverStart(ServerAboutToStartEvent e) {
//        MixRegistry.applyMixes();
//    }

    public static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(this::registerCommands);
        register();
        ServerLifecycleEvents.SERVER_STARTING.register(s -> MixRegistry.applyMixes());
        registerServerReloadListener();
        setup();
        PlaceboConfig.load();
    }
}

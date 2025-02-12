package dev.shadowsoffire.placebo;

import dev.shadowsoffire.placebo.events.ResourceReloadCallback;
import dev.shadowsoffire.placebo.patreon.TrailsManager;
import dev.shadowsoffire.placebo.patreon.WingsManager;
import dev.shadowsoffire.placebo.patreon.wings.Wing;
import dev.shadowsoffire.placebo.patreon.wings.WingLayer;
import io.github.cputnama11y.patch.duck.EntityRenderDispatcherAccessor;
import io.github.cputnama11y.patch.mixin.LivingEntityRendererAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

//@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD, modid = Placebo.MODID)
public class PlaceboClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        setup();
    }

    public static long ticks = 0;

    //    @SubscribeEvent
    public static void setup() {
        TrailsManager.init();
        WingsManager.init();
        ClientTickEvents.END_CLIENT_TICK.register(PlaceboClient::tick);
        KeyBindingHelper.registerKeyBinding(TrailsManager.TOGGLE);
        KeyBindingHelper.registerKeyBinding(WingsManager.TOGGLE);
        clientResource();
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(PlaceboClient::addLayers);
    }

    public static void clientResource() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {
            final ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Placebo.MODID, "client_reload");

            @Override
            public ResourceLocation getFabricId() {
                return id;
            }

            @Override
            @NotNull
            public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) {
                return CompletableFuture.runAsync(
                        () -> ResourceReloadCallback.EVENT.invoker().onResourceReload(resourceManager, true)
                );
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static void addLayers(EntityType<? extends LivingEntity> entityType, LivingEntityRenderer<?, ?> entityRenderer, LivingEntityFeatureRendererRegistrationCallback.RegistrationHelper registrationHelper, EntityRendererProvider.Context context) {
        Wing.INSTANCE = new Wing(context.bakeLayer(WingsManager.WING_LOC));
        EntityRenderDispatcherAccessor dispatcher = (EntityRenderDispatcherAccessor) context.getEntityRenderDispatcher();
        for (PlayerSkin.Model s : dispatcher.placebo$getModels()) {
            LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> skin =
                    (LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>)
                            dispatcher.placebo$getSkin(s);
            var accessor1 = ((LivingEntityRendererAccessor<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>) skin);
            accessor1.placebo$addLayer(new WingLayer(skin));
        }
    }

    public static void tick(Minecraft e) {
        ticks++;
    }

    public static float getColorTicks() {
        return (ticks + Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false)) / 0.5F;
    }

    @Nullable
    public static PotionBrewing getBrewingRegistry() {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null
               ? null
               : level.potionBrewing();
    }
}

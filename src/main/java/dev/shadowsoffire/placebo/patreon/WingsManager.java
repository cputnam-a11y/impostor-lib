package dev.shadowsoffire.placebo.patreon;

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.patreon.PatreonUtils.WingType;
import dev.shadowsoffire.placebo.payloads.PatreonDisablePayload;
import dev.shadowsoffire.placebo.payloads.PatreonDisablePayload.CosmeticType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import org.lwjgl.glfw.GLFW;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.*;
public class WingsManager {

    static Map<UUID, WingType> WINGS = new HashMap<>();
    public static final KeyMapping TOGGLE = new KeyMapping("placebo.toggleWings", GLFW.GLFW_KEY_KP_8, "key.categories.placebo");
    public static final Set<UUID> DISABLED = new HashSet<>();
    public static final ModelLayerLocation WING_LOC = new ModelLayerLocation(Placebo.loc("wings"), "main");

    public static void init() {
        new Thread(() -> {
            Placebo.LOGGER.info("Loading patreon wing data...");
            try {
                URL url = new URI("https://raw.githubusercontent.com/Shadows-of-Fire/Placebo/1.16/PatreonWings.txt").toURL();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                    String s;
                    while ((s = reader.readLine()) != null) {
                        String[] split = s.split(" ", 2);
                        if (split.length != 2) {
                            Placebo.LOGGER.error("Invalid patreon wing entry {} will be ignored.", s);
                            continue;
                        }
                        WINGS.put(UUID.fromString(split[0]), WingType.valueOf(split[1]));
                    }
                    reader.close();
                }
                catch (IOException ex) {
                    Placebo.LOGGER.error("Exception loading patreon wing data!");
                    ex.printStackTrace();
                }
            }
            catch (Exception k) {
                // not possible
            }
            Placebo.LOGGER.info("Loaded {} patreon wings.", WINGS.size());
            if (!WINGS.isEmpty()) ClientTickEvents.END_CLIENT_TICK.register(WingsManager::tickKeys);
        }, "Placebo Patreon Wing Loader").start();
    }

    public static void tickKeys(Minecraft minecraft) {
        if (TOGGLE.consumeClick()) {
            ClientPlayNetworking.send(new PatreonDisablePayload(CosmeticType.WINGS, Minecraft.getInstance().player.getUUID()));
        }
    }

    public static WingType getType(UUID id) {
        return WINGS.get(id);
    }

}

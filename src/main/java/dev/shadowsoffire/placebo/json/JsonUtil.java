package dev.shadowsoffire.placebo.json;

import com.google.gson.JsonElement;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Logger;


public class JsonUtil {

    /**
     * Checks if an item is empty, and if it is, returns false and logs the key.
     */
    public static boolean checkAndLogEmpty(JsonElement e, ResourceLocation id, String type, Logger logger) {
        String s = e.toString();
        if (s.isEmpty() || "{}".equals(s)) {
            logger.error("Ignoring {} item with id {} as it is empty.  Please switch to a condition-false json instead of an empty one.", type, id);
            return false;
        }
        return true;
    }

    /**
     * Checks the conditions on a Json, and returns true if they are met.
     * Only checks 'neoforge:conditions' as of 1.20.4.
     *
     * @param e       The Json being checked.
     * @param id      The ID of that json.
     * @param type    The type of the json, for logging.
     * @param logger  The logger to log to.
     * @param context The context object used for resolving conditions.
     * @return True if the item's conditions are met, false otherwise.
     */
    public static boolean checkConditions(JsonElement e, ResourceLocation id, String type, Logger logger, RegistryOps<JsonElement> ops) {
        if (ResourceCondition.CONDITION_CODEC.parse(ops, e.getAsJsonObject()).result().map(r -> r.test(null /*get registries from ops. it possible, requires copious amounts of mixin*/)).orElse(false)) {
            return true;
        }
        logger.trace("Skipping loading {} item with id {} as it's conditions were not met", type, id);
        return false;
    }

}

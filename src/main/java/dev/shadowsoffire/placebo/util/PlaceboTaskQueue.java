package dev.shadowsoffire.placebo.util;

import dev.shadowsoffire.placebo.Placebo;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

/**
 * Helper class for scheduling transient tick-based tasks on the server.
 * <p>
 * Do not use for critical functionality, since the queue is abandoned entirely if the game closes or crashes.
 */
public class PlaceboTaskQueue {

    /**
     * Submits a new task for immediate execution.
     */
    public static void submitTask(ResourceLocation id, Task task) {
        Impl.TASKS.add(Pair.of(id, task));
    }

    /**
     * Submits a new task for delayed execution.
     * 
     * @param delay The delay, in ticks, before the task begins executing.
     */
    public static void submitDelayedTask(ResourceLocation id, int delay, Task task) {
        Impl.TASKS.add(Pair.of(id, new DelayedTask(delay, task)));
    }

    @FunctionalInterface
    public static interface Task {

        /**
         * Executes the task, returning a status specifying if the task finished or not.
         * 
         * @return The completion status, either {@link Status#RUNNING} to continue executing or {@link Status#COMPLETED} to stop.
         */
        Status execute();
    }

    public static enum Status {
        RUNNING,
        COMPLETED;

        public boolean isCompleted() {
            return this == COMPLETED;
        }
    }

    private static class DelayedTask implements Task {

        private int delay;
        private Task task;

        private DelayedTask(int delay, Task task) {
            this.delay = delay;
            this.task = task;
        }

        @Override
        public Status execute() {
            if (delay-- > 0) {
                return Status.COMPLETED;
            }
            return this.task.execute();
        }

    }

//    @EventBusSubscriber(modid = Placebo.MODID, bus = Bus.GAME)
    public static class Impl {
        static {
            ServerLifecycleEvents.SERVER_STARTING.register(Impl::started);
            ServerLifecycleEvents.SERVER_STOPPING.register(Impl::stopped);
            ServerTickEvents.END_SERVER_TICK.register(Impl::tick);
        }
        private static final Queue<Pair<ResourceLocation, Task>> TASKS = new ArrayDeque<>();

//        @SubscribeEvent
        public static void tick(MinecraftServer server) {
            Iterator<Pair<ResourceLocation, Task>> it = TASKS.iterator();
            Pair<ResourceLocation, Task> current = null;
            while (it.hasNext()) {
                current = it.next();
                try {
                    if (current.getRight().execute().isCompleted()) {
                        it.remove();
                    }
                }
                catch (Exception ex) {
                    Placebo.LOGGER.error("An exception occurred while running a ticking task with ID {}. It will be terminated.", current.getLeft());
                    it.remove();
                    ex.printStackTrace();
                }
            }
        }

        public static void stopped(MinecraftServer server) {
            TASKS.clear();
        }

        public static void started(MinecraftServer server) {
            TASKS.clear();
        }
    }

}

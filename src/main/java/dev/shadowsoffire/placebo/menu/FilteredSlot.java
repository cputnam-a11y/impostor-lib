package dev.shadowsoffire.placebo.menu;

import java.util.function.Predicate;

import com.google.common.base.Predicates;

import dev.shadowsoffire.placebo.cap.InternalItemHandler;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Extension of {@link Slot} which takes a filter on what may enter the slot.
 */
public class FilteredSlot extends Slot {

    protected final Predicate<ItemStack> filter;
    protected final int index;

    /**
     * Creates a new filtered slot
     *
     * @param Container The backing item handler
     * @param index   The slot index
     * @param x       The x coordinate
     * @param y       The y coordinate
     * @param filter  A filter controlling what items may be placed in the slot by a player
     */
    public FilteredSlot(Container Container, int index, int x, int y, Predicate<ItemStack> filter) {
        super(Container, index, x, y);
        this.filter = filter;
        this.index = index;
    }

    public FilteredSlot(Container container, int index, int x, int y) {
        this(container, index, x, y, Predicates.alwaysTrue());
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.filter.test(stack);
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return super.mayPickup(playerIn);
    }
}

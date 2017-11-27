package com.technicalitiesmc.lib.inventory.widget;

import elec332.core.inventory.widget.slot.WidgetSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

/**
 * Stolen from Random Things.
 *
 * @author lumien231
 */
public class WidgetGhostSlot extends WidgetSlot {

    public WidgetGhostSlot(IItemHandler inventory, int index, int x, int y, boolean stackable) {
        super(inventory, index, x, y);
        this.stackable = stackable;
    }

    private final boolean stackable;

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        ItemStack holding = player.inventory.getItemStack();
        holding = holding.copy();
        if (!holding.isEmpty() && !stackable) {
            holding.setCount(1);
        }
        this.putStack(holding);
        return false;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        stack = stack.copy();
        if (!stackable) {
            stack.setCount(1);
        }
        this.putStack(stack);
        return false;
    }

    @Nonnull
    @Override
    public ItemStack decrStackSize(int amount) {
        this.putStack(ItemStack.EMPTY);
        return ItemStack.EMPTY;
    }

}

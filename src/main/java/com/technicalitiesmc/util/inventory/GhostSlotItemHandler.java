package com.technicalitiesmc.util.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * Stolen from Random Things.
 *
 * @author lumien231
 */
public class GhostSlotItemHandler extends SlotItemHandler {

    private final boolean stackable;

    public GhostSlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition, boolean stackable) {
        super(itemHandler, index, xPosition, yPosition);
        this.stackable = stackable;
    }

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

    @Override
    public ItemStack decrStackSize(int amount) {
        this.putStack(ItemStack.EMPTY);
        return ItemStack.EMPTY;
    }
}

package com.technicalitiesmc.util.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class InsertingSlotItemHandler extends SlotItemHandler {

    public InsertingSlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public void putStack(ItemStack stack) {
        this.getItemHandler().extractItem(getSlotIndex(), 64, false);
        this.getItemHandler().insertItem(getSlotIndex(), stack, false);
        this.onSlotChanged();
    }

}

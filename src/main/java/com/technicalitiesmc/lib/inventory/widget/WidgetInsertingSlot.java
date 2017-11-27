package com.technicalitiesmc.lib.inventory.widget;

import elec332.core.inventory.widget.slot.WidgetSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

/**
 * Created by Elec332 on 27-11-2017.
 */
public class WidgetInsertingSlot extends WidgetSlot {

    public WidgetInsertingSlot(IItemHandler inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public void putStack(@Nonnull ItemStack stack) {
        this.getInventory().extractItem(getSlotIndex(), 64, false);
        this.getInventory().insertItem(getSlotIndex(), stack, false);
        this.onSlotChanged();
    }

}

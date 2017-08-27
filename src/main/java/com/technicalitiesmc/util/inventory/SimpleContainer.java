package com.technicalitiesmc.util.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public class SimpleContainer extends Container {

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    @Override
    public Slot addSlotToContainer(Slot slot) {
        return super.addSlotToContainer(slot);
    }

}

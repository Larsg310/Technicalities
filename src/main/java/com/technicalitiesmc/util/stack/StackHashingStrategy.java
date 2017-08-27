package com.technicalitiesmc.util.stack;

import gnu.trove.strategy.HashingStrategy;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemHandlerHelper;

public enum StackHashingStrategy implements HashingStrategy<ItemStack> {
    INSTANCE;

    @Override
    public int computeHashCode(ItemStack stack) {
        NBTTagCompound tag = stack.serializeNBT();
        tag.removeTag("Count"); // We don't care about stacksize, only if the items could stack
        return tag.hashCode();
    }

    @Override
    public boolean equals(ItemStack o1, ItemStack o2) {
        return ItemHandlerHelper.canItemStacksStack(o1, o2);
    }

}

package com.technicalitiesmc.mechanical.conveyor.object;

import com.technicalitiesmc.api.mechanical.conveyor.IConveyorObject;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

public class ConveyorStack extends ConveyorObject implements IConveyorObject.Stack {
    private ItemStack stack;

    public ConveyorStack(ItemStack stack) {
        this.stack = stack;
    }

    public ConveyorStack() { }

    @Override
    public float width() {
        return 0.25f;
    }

    @Nonnull
    @Override
    public ItemStack getStack() {
        return stack.copy();
    }

    @Override
    public boolean shouldJoinFromOutside() {
        return true;
    }

    @Override
    public boolean requiresBigConveyor() {
        return false;
    }

    @Override
    public boolean requiresStickyConveyor() {
        return false;
    }

    @Override
    public void saveData(NBTTagCompound tag) {
        super.saveData(tag);
        stack.writeToNBT(tag);
    }

    @Override
    public void loadData(NBTTagCompound tag) {
        super.loadData(tag);
        stack = new ItemStack(tag);
    }
}

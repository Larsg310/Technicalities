package com.technicalitiesmc.mechanical.conveyor.object;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3i;

public class ConveyorStack extends ConveyorObject {

    private static final Vec3i ONE = new Vec3i(1, 1, 1);

    private ItemStack stack;

    public ConveyorStack(ItemStack stack) {
        this.stack = stack;
    }

    public ConveyorStack() {
    }

    @Override
    public Vec3i getDimensions() {
        return ONE;
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
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        return stack.writeToNBT(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        stack = new ItemStack(tag);
    }

}

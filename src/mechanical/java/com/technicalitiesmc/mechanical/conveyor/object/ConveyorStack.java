package com.technicalitiesmc.mechanical.conveyor.object;

import com.technicalitiesmc.api.mechanical.conveyor.IConveyorObject;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

public class ConveyorStack extends ConveyorObject implements IConveyorObject.Stack {
    private static final Collection<AxisAlignedBB> BOUNDS = Collections.singleton(new AxisAlignedBB(-0.25, 0, -0.25, 0.25, 0.5, 0.25));

    private ItemStack stack;

    public ConveyorStack(ItemStack stack) {
        this.stack = stack;
    }

    public ConveyorStack() { }

    @Nonnull
    @Override
    public ItemStack getStack() {
        return stack.copy();
    }

    @Nonnull
    @Override
    public Collection<AxisAlignedBB> bounds() {
        return BOUNDS;
    }

    @Override
    public ItemStack getDropItem() {
        return stack;
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

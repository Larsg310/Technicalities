package com.technicalitiesmc.api.mechanical.conveyor;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3i;

public interface IConveyorObject {

    public Vec3i getDimensions();

    public boolean shouldJoinFromOutside();

    public boolean requiresBigConveyor();

    public boolean requiresStickyConveyor();

    public NBTTagCompound writeToNBT(NBTTagCompound tag);

    public void readFromNBT(NBTTagCompound tag);

    public static interface Stack extends IConveyorObject {

        public ItemStack getStack();

    }

    public static interface Block extends IConveyorObject {

        public IBlockState getState();

    }

}

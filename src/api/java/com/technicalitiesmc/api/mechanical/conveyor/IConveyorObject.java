package com.technicalitiesmc.api.mechanical.conveyor;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.UUID;

public interface IConveyorObject {
    @Nonnull
    public UUID uuid();

    @Nonnull
    public Collection<AxisAlignedBB> bounds();

    public boolean shouldJoinFromOutside();

    public boolean requiresBigConveyor();

    public boolean requiresStickyConveyor();

    public void saveData(NBTTagCompound tag);

    public void loadData(NBTTagCompound tag);

    // TODO: temporary -- replace with Entity?
    public ItemStack getDropItem();

    public static interface Stack extends IConveyorObject {
        @Nonnull
        public ItemStack getStack();
    }

    public static interface Block extends IConveyorObject {
        @Nonnull
        public IBlockState getState();
    }
}

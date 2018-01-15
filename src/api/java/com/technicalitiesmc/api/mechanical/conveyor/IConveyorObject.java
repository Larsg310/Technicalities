package com.technicalitiesmc.api.mechanical.conveyor;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface IConveyorObject {
    @Nonnull
    public UUID uuid();

    // the width (1.0 is the width of a full block)
    public float width();

    public boolean shouldJoinFromOutside();

    public boolean requiresBigConveyor();

    public boolean requiresStickyConveyor();

    public void saveData(NBTTagCompound tag);

    public void loadData(NBTTagCompound tag);

    public static interface Stack extends IConveyorObject {
        @Nonnull
        public ItemStack getStack();
    }

    public static interface Block extends IConveyorObject {
        @Nonnull
        public IBlockState getState();
    }
}

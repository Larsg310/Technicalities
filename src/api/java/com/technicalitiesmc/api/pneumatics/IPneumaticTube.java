package com.technicalitiesmc.api.pneumatics;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

public interface IPneumaticTube {

    public World getTubeWorld();

    public BlockPos getTubePos();

    public boolean isConnected(EnumFacing side);

    public INeighbor getNeighbor(EnumFacing side);

    public default void insertStack(EnumFacing side, ItemStack stack, EnumDyeColor color) {
        insertStack(side, 0, EnumTubeDirection.INWARDS, stack, color);
    }

    public void insertStack(EnumFacing side, float position, EnumTubeDirection direction, ItemStack stack, EnumDyeColor color);

    public void removeStack(ITubeStack stack);

    public void save();

    public void sync();

    public interface INeighbor {

        public boolean isInventory();

        public boolean isTube();

        public IItemHandler asInventory();

        public IPneumaticTube asTube();

    }

}

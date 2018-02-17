package com.technicalitiesmc.api.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface IBlockWrenchable {
    /**
     * Gets called when the block is hit by a wrench.
     * Use only if the Block.rotateBlock(World, BlockPos, EnumFacing) function is not enough, for compatibility!
     */
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis, @Nullable EntityPlayer player, float hitX, float hitY, float hitZ);
}

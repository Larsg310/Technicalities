package com.technicalitiesmc.mechanical.block;

import com.technicalitiesmc.util.block.BlockBase;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockGrate extends BlockBase {

    private final AxisAlignedBB AABB = new AxisAlignedBB(0, 14 / 16D, 0, 1, 1, 1);

    public BlockGrate() {
        super(Material.IRON);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return AABB;
    }

    @Override
    protected boolean isFull(IBlockState state) {
        return false;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT;
    }

}

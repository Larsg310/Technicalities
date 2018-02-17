package com.technicalitiesmc.base.block;

import com.technicalitiesmc.lib.block.BlockBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class BlockFunnel extends BlockBase {
    public static final Collection<AxisAlignedBB> COLLISION = Arrays.asList(
        new AxisAlignedBB(0, 0.4375, 0, 0.0625, 1, 1),
        new AxisAlignedBB(0, 0.4375, 0, 1, 1, 0.0625),
        new AxisAlignedBB(0.9375, 0.4375, 0, 1, 1, 1),
        new AxisAlignedBB(0, 0.4375, 0.9375, 1, 1, 1),
        new AxisAlignedBB(0, 0, 0, 0.3125, 0.4375, 1),
        new AxisAlignedBB(0.6875, 0, 0, 1, 0.4375, 1),
        new AxisAlignedBB(0, 0, 0, 1, 0.4375, 0.3125),
        new AxisAlignedBB(0, 0, 0.6875, 1, 0.4375, 1)
    );

    public BlockFunnel() {
        super(Material.GLASS);
        setHardness(1.0f);
    }

    @Override
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        super.onEntityCollidedWithBlock(worldIn, pos, state, entityIn);
        Vec3d toCenter = new Vec3d(pos).addVector(0.5, 0.5, 0.5).subtract(entityIn.getPositionVector());
        entityIn.motionX = entityIn.motionX * 0.5 + toCenter.x;
        entityIn.motionY = Math.max(-0.2, entityIn.motionY);
        entityIn.motionZ = entityIn.motionZ * 0.5 + toCenter.z;
    }

    @Override
    public void addCollisionBoxes(IBlockState state, World world, BlockPos pos, List<AxisAlignedBB> boxes) {
        boxes.addAll(COLLISION);
    }

    @Override
    protected boolean isFull(IBlockState state) {
        return false;
    }
}

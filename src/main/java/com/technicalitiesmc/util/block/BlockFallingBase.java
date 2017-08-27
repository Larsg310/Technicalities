package com.technicalitiesmc.util.block;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BlockFallingBase extends BlockFalling {

    public BlockFallingBase(Material material) {
        super(material);
    }

    @Override
    public Block setSoundType(SoundType sound) {
        return super.setSoundType(sound);
    }

    @Override
    public String getUnlocalizedName() {
        return "tile." + getRegistryName();
    }

    public void addBoxes(IBlockState state, World world, BlockPos pos, List<AxisAlignedBB> boxes) {
        boxes.add(state.getBoundingBox(world, pos));
    }

    public void addSelectionBoxes(IBlockState state, World world, BlockPos pos, List<AxisAlignedBB> boxes) {
        addBoxes(state, world, pos, boxes);
    }

    @Override
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
        List<AxisAlignedBB> boxes = new ArrayList<>();
        addSelectionBoxes(state, world, pos, boxes);
        return boxes.stream().reduce(null, (prev, box) -> {
            RayTraceResult hit = rayTrace(pos, start, end, box);
            return prev != null && (hit == null || start.squareDistanceTo(prev.hitVec) < start.squareDistanceTo(hit.hitVec)) ? prev : hit;
        }, (a, b) -> b);
    }

    protected boolean isFull(IBlockState state) {
        return true;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return isFull(state);
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return isFull(state);
    }

    @Override
    public boolean isNormalCube(IBlockState state) {
        return isFull(state);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return isFull(state);
    }

    protected boolean canBreak(World world, BlockPos pos, EntityPlayer player) {
        return true;
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        if (!canBreak(world, pos, player)) {
            if (player.capabilities.isCreativeMode) {
                onBlockClicked(world, pos, player);
            }
            return false;
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

}

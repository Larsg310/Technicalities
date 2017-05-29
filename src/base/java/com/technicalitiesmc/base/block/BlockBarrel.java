package com.technicalitiesmc.base.block;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.technicalitiesmc.base.tile.TileBarrel;
import com.technicalitiesmc.lib.block.BlockBase;
import com.technicalitiesmc.lib.math.RayTraceHelper;

import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockBarrel extends BlockBase implements ITileEntityProvider {

    private static final EnumFacing.Axis[] AXIS_VALUES = EnumFacing.Axis.values();
    private static final AxisAlignedBB[][] BOXES = new AxisAlignedBB[][] { //
            { //
                    new AxisAlignedBB(0 / 16D, 0 / 16D, 4 / 16D, 16 / 16D, 16 / 16D, 12 / 16D), //
                    new AxisAlignedBB(0 / 16D, 1 / 16D, 2 / 16D, 16 / 16D, 15 / 16D, 14 / 16D), //
                    new AxisAlignedBB(0 / 16D, 2 / 16D, 1 / 16D, 16 / 16D, 14 / 16D, 15 / 16D), //
                    new AxisAlignedBB(0 / 16D, 4 / 16D, 0 / 16D, 16 / 16D, 12 / 16D, 16 / 16D)//
            }, { //
                    new AxisAlignedBB(4 / 16D, 0 / 16D, 0 / 16D, 12 / 16D, 16 / 16D, 16 / 16D), //
                    new AxisAlignedBB(2 / 16D, 0 / 16D, 1 / 16D, 14 / 16D, 16 / 16D, 15 / 16D), //
                    new AxisAlignedBB(1 / 16D, 0 / 16D, 2 / 16D, 15 / 16D, 16 / 16D, 14 / 16D), //
                    new AxisAlignedBB(0 / 16D, 0 / 16D, 4 / 16D, 16 / 16D, 16 / 16D, 12 / 16D)//
            }, { //
                    new AxisAlignedBB(4 / 16D, 0 / 16D, 0 / 16D, 12 / 16D, 16 / 16D, 16 / 16D), //
                    new AxisAlignedBB(2 / 16D, 1 / 16D, 0 / 16D, 14 / 16D, 15 / 16D, 16 / 16D), //
                    new AxisAlignedBB(1 / 16D, 2 / 16D, 0 / 16D, 15 / 16D, 14 / 16D, 16 / 16D), //
                    new AxisAlignedBB(0 / 16D, 4 / 16D, 0 / 16D, 16 / 16D, 12 / 16D, 16 / 16D)//
            } };

    public BlockBarrel() {
        super(Material.WOOD);
        setHardness(3.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileBarrel(32);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(BlockRotatedPillar.AXIS).build();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BlockRotatedPillar.AXIS, AXIS_VALUES[meta]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BlockRotatedPillar.AXIS).ordinal();
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta,
            EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(BlockRotatedPillar.AXIS, EnumFacing.getDirectionFromEntityLiving(pos, placer).getAxis());
    }

    @Override
    public void addBoxes(IBlockState state, World world, BlockPos pos, List<AxisAlignedBB> boxes) {
        for (AxisAlignedBB box : BOXES[state.getValue(BlockRotatedPillar.AXIS).ordinal()]) {
            boxes.add(box);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing,
            float hitX, float hitY, float hitZ) {
        if (facing.getAxis() == state.getValue(BlockRotatedPillar.AXIS)) {
            ItemStack stack = player.getHeldItem(hand);
            if (!stack.isEmpty()) {
                if (!world.isRemote) {
                    TileBarrel tile = (TileBarrel) world.getTileEntity(pos);
                    ItemStack leftover = tile.insertItem(0, stack, false);
                    player.setHeldItem(hand, leftover);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
        IBlockState state = world.getBlockState(pos);
        Pair<Vec3d, Vec3d> vec = RayTraceHelper.getRayTraceVectors(player);
        RayTraceResult hit = collisionRayTrace(state, world, pos, vec.getLeft(), vec.getRight());
        if (hit == null || hit.sideHit.getAxis() != state.getValue(BlockRotatedPillar.AXIS)) {
            return;
        }
        if (!world.isRemote) {
            TileBarrel tile = (TileBarrel) world.getTileEntity(pos);
            ItemStack extracted = tile.extractItem(0, player.isSneaking() ? 64 : 1, false);
            ItemHandlerHelper.giveItemToPlayer(player, extracted);
        }
    }

    @Override
    protected boolean canBreak(World world, BlockPos pos, EntityPlayer player) {
        if (player.isSneaking()) {
            return true;
        }

        TileBarrel tile = (TileBarrel) world.getTileEntity(pos);
        if (tile.getAmount() == 0) {
            return true;
        }

        IBlockState state = world.getBlockState(pos);
        Pair<Vec3d, Vec3d> vec = RayTraceHelper.getRayTraceVectors(player);
        RayTraceResult hit = collisionRayTrace(state, world, pos, vec.getLeft(), vec.getRight());
        if (hit == null || hit.sideHit.getAxis() != state.getValue(BlockRotatedPillar.AXIS)) {
            return true;
        }

        return false;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileBarrel tile = (TileBarrel) world.getTileEntity(pos);
        ItemStack stack = tile.getType().copy();
        stack.setCount(tile.getAmount());
        while (!stack.isEmpty()) {
            InventoryHelper.spawnItemStack(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack.splitStack(64));
        }
        world.updateComparatorOutputLevel(pos, this);

        super.breakBlock(world, pos, state);
    }

    @Override
    protected boolean isFull(IBlockState state) {
        return false;
    }

}

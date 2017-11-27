package com.technicalitiesmc.mechanical.block;

import com.technicalitiesmc.lib.block.BlockBase;
import com.technicalitiesmc.mechanical.tile.TileShaft;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockShaft extends BlockBase implements ITileEntityProvider {

    private static final AxisAlignedBB[] BOXES = new AxisAlignedBB[] { //
            new AxisAlignedBB(0, 6 / 16D, 6 / 16D, 1, 10 / 16D, 10 / 16D), //
            new AxisAlignedBB(6 / 16D, 0, 6 / 16D, 10 / 16D, 1, 10 / 16D), //
            new AxisAlignedBB(6 / 16D, 6 / 16D, 0, 10 / 16D, 10 / 16D, 1)//
    };

    public BlockShaft() {
        super(Material.WOOD);
        setDefaultState(getDefaultState().withProperty(BlockRotatedPillar.AXIS, EnumFacing.Axis.Y));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileShaft();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(BlockRotatedPillar.AXIS).build();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BlockRotatedPillar.AXIS, EnumFacing.Axis.values()[meta]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BlockRotatedPillar.AXIS).ordinal();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, RayTraceResult hit) {
        if (hit.sideHit.getAxis() != state.getValue(BlockRotatedPillar.AXIS)) {
            if (!world.isRemote) {
                ((TileShaft) world.getTileEntity(pos)).debug();
            }
            return true;
        }
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOXES[state.getValue(BlockRotatedPillar.AXIS).ordinal()];
    }

    @Override
    protected boolean isFull(IBlockState state) {
        return false;
    }


    @Nonnull
    @Override
    public IBlockState getBlockStateForPlacementC(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, @Nullable EnumHand hand) {
        return getDefaultState().withProperty(BlockRotatedPillar.AXIS, facing.getAxis());
    }

}

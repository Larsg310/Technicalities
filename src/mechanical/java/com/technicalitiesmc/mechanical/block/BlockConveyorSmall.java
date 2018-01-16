package com.technicalitiesmc.mechanical.block;

import com.technicalitiesmc.lib.block.BlockBase;
import com.technicalitiesmc.mechanical.tile.TileConveyorSmall;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockConveyorSmall extends BlockBase implements ITileEntityProvider {
    private static final AxisAlignedBB AABB = new AxisAlignedBB(0, 0, 0, 1, 9 / 16D, 1);
    public static final IProperty<Integer> PROPERTY_ROTATION = PropertyInteger.create("rotation", 0, 1);

    public BlockConveyorSmall() {
        super(Material.IRON);
        setDefaultState(getDefaultState().withProperty(PROPERTY_ROTATION, 0));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileConveyorSmall();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(PROPERTY_ROTATION).build();
    }

    @Override
    public boolean onBlockActivatedC(World world, BlockPos pos, EntityPlayer player, EnumHand hand, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileConveyorSmall tileEntity = (TileConveyorSmall) world.getTileEntity(pos);
        tileEntity.b ^= true;
        return true;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(PROPERTY_ROTATION, meta);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(PROPERTY_ROTATION);
    }

    @Nonnull
    @Override
    public IBlockState getBlockStateForPlacementC(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, @Nullable EnumHand hand) {
        return getDefaultState().withProperty(PROPERTY_ROTATION, placer.getHorizontalFacing().getAxis() == Axis.X ? 1 : 0);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return AABB;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

//    @Override
//    public void onFallenUpon(World world, BlockPos pos, Entity entity, float fallDistance) { }
}

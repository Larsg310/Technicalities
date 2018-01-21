package com.technicalitiesmc.mechanical.block;

import com.technicalitiesmc.api.mechanical.conveyor.IConveyorBelt;
import com.technicalitiesmc.lib.block.BlockBase;
import com.technicalitiesmc.mechanical.tile.TileConveyorBase;
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
import java.util.Objects;

public abstract class BlockConveyorBase extends BlockBase implements ITileEntityProvider {
    private final AxisAlignedBB AABB;
    public static final IProperty<Integer> PROPERTY_ROTATION = PropertyInteger.create("rotation", 0, 1);

    public BlockConveyorBase(float height) {
        super(Material.IRON);
        AABB = new AxisAlignedBB(0, 0, 0, 1, height, 1);
        setDefaultState(getDefaultState().withProperty(PROPERTY_ROTATION, 0));
    }

    @Nullable
    @Override
    public abstract TileEntity createNewTileEntity(World worldIn, int meta);

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(PROPERTY_ROTATION).build();
    }

    @Override
    public boolean onBlockActivatedC(World world, BlockPos pos, EntityPlayer player, EnumHand hand, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileConveyorBase tileEntity = (TileConveyorBase) world.getTileEntity(pos);
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

    // TODO: is this the right method for dropping the items?
    @Override
    public boolean removedByPlayer(@Nonnull IBlockState state, World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player, boolean willHarvest) {
        TileEntity te = world.getTileEntity(pos);
        if (te != null && te.hasCapability(IConveyorBelt.CAPABILITY, null))
            Objects.requireNonNull(te.getCapability(IConveyorBelt.CAPABILITY, null)).onHostDestroyed();
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    //    @Override
//    public void onFallenUpon(World world, BlockPos pos, Entity entity, float fallDistance) { }
}

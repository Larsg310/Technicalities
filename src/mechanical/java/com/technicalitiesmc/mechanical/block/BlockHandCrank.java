package com.technicalitiesmc.mechanical.block;

import com.technicalitiesmc.lib.block.BlockBase;
import com.technicalitiesmc.mechanical.tile.TileHandCrank;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class BlockHandCrank extends BlockBase implements ITileEntityProvider {
    public static final List<AxisAlignedBB> COLLISION = Arrays.asList(
        new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.25, 1.0),
        new AxisAlignedBB(0.0, 0.75, 0.0, 1.0, 1.0, 1.0),
        new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 0.25),
        new AxisAlignedBB(0.0, 0.0, 0.75, 1.0, 1.0, 1.0),
        new AxisAlignedBB(0.0, 0.0, 0.0, 0.25, 1.0, 1.0),
        new AxisAlignedBB(0.75, 0.0, 0.0, 1.0, 1.0, 1.0)
    );

    public static final PropertyEnum<EnumFacing> PROP_FACING = PropertyEnum.create("facing", EnumFacing.class);

    public BlockHandCrank() {
        super(Material.ROCK);
        setDefaultState(getBlockState().getBaseState().withProperty(PROP_FACING, EnumFacing.DOWN));
    }

    @Override
    public boolean onBlockActivatedC(World world, BlockPos pos, EntityPlayer player, EnumHand hand, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ((TileHandCrank) world.getTileEntity(pos)).crank(player);
        return true;
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, PROP_FACING);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(PROP_FACING).getIndex();
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(PROP_FACING, EnumFacing.getFront(meta));
    }

    @Nonnull
    @Override
    public IBlockState getBlockStateForPlacementC(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, @Nullable EnumHand hand) {
        return getDefaultState().withProperty(PROP_FACING, facing.getOpposite());
    }

    @Override
    protected boolean isFull(IBlockState state) {
        return false;
    }

    @Override
    public void addCollisionBoxes(IBlockState state, World world, BlockPos pos, List<AxisAlignedBB> boxes) {
        boxes.add(COLLISION.get(state.getValue(PROP_FACING).getIndex()));
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
        return new TileHandCrank();
    }
}

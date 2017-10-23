package com.technicalitiesmc.mechanical.block;

import com.technicalitiesmc.util.block.BlockBase;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockConveyor extends BlockBase {

    public static final IProperty<Integer> PROPERTY_ROTATION = PropertyInteger.create("rotation", 0, 1);

    public BlockConveyor() {
        super(Material.IRON);
        setDefaultState(getDefaultState().withProperty(PROPERTY_ROTATION, 0));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(PROPERTY_ROTATION).build();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(PROPERTY_ROTATION, meta);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(PROPERTY_ROTATION);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta,
            EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(PROPERTY_ROTATION, placer.getHorizontalFacing().getAxis() == Axis.X ? 1 : 0);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

}

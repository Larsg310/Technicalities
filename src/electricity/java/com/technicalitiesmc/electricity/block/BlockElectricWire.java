package com.technicalitiesmc.electricity.block;

import com.technicalitiesmc.electricity.tile.TileElectricWire;
import com.technicalitiesmc.lib.block.BlockBase;
import elec332.core.util.UniversalUnlistedProperty;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nullable;

public class BlockElectricWire extends BlockBase implements ITileEntityProvider {

    private static final IUnlistedProperty<Integer> PROPERTY_COLORS = new UniversalUnlistedProperty<>("colors", Integer.class);

    public BlockElectricWire() {
        super(Material.CIRCUITS);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileElectricWire();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(PROPERTY_COLORS).build();
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        IExtendedBlockState iebs = (IExtendedBlockState) state;
        return iebs.withProperty(PROPERTY_COLORS, getTile(world, pos, TileElectricWire.class).getColorBits());
    }

}

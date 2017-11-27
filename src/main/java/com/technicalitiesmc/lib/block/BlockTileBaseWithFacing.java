package com.technicalitiesmc.lib.block;

import elec332.core.client.model.loading.INoBlockStateJsonBlock;
import elec332.core.tile.BlockTileBase;
import elec332.core.util.BlockStateHelper;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Created by Elec332 on 23-11-2017.
 */
public class BlockTileBaseWithFacing extends BlockTileBase implements INoBlockStateJsonBlock.RotationImpl {

	public BlockTileBaseWithFacing(Material mat, Class<? extends TileEntity> tileClass, ResourceLocation name) {
		super(mat, tileClass, name);
	}

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BlockStateHelper.FACING_NORMAL.getProperty());
	}

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public IBlockState getStateFromMeta(int meta) {
		return BlockStateHelper.FACING_NORMAL.getStateForMeta(this, meta);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return BlockStateHelper.FACING_NORMAL.getMetaForState(state);
	}

}

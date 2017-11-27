package com.technicalitiesmc.base.block;

import com.technicalitiesmc.base.tile.TileWorkbench;
import com.technicalitiesmc.lib.block.BlockBase;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockWorkbench extends BlockBase implements ITileEntityProvider {

    public BlockWorkbench() {
        super(Material.ROCK);
    }

    @Override
    public TileEntity createNewTileEntity(@Nonnull World world, int meta) {
        return new TileWorkbench();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, RayTraceResult hit) {
        if (!world.isRemote) {
            openTileWindow(player, world, pos);
        }
        return true;
    }

}

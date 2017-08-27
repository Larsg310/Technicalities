package com.technicalitiesmc.mechanical.block;

import com.technicalitiesmc.mechanical.tile.TileKineticTest;
import com.technicalitiesmc.util.block.BlockBase;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class BlockKineticTest extends BlockBase implements ITileEntityProvider {

    public BlockKineticTest() {
        super(Material.ROCK);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileKineticTest();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, RayTraceResult hit) {
        if (!world.isRemote && hit.sideHit.getAxis() != EnumFacing.Axis.Y) {
            ((TileKineticTest) world.getTileEntity(pos)).debug();
        }
        return hit.sideHit.getAxis() != EnumFacing.Axis.Y;
    }

}

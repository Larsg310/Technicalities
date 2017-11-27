package com.technicalitiesmc.base.block;

import com.technicalitiesmc.base.tile.TileChannel;
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
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

public class BlockChannel extends BlockBase implements ITileEntityProvider {
    public BlockChannel() {
        super(Material.ROCK);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, RayTraceResult hit) {
        //if (world.isRemote) return true;
        if (player.isSneaking()) {
            if (world.isRemote) return true;
            TileEntity te = world.getTileEntity(pos);
            if (te != null) {
                TileChannel tc = (TileChannel)te;
                IFluidHandler tank = tc.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, hit.sideHit);
                tank.fill(new FluidStack(FluidRegistry.WATER, 500), true);
                te.markDirty();
            }
        } else {
            TileEntity te = world.getTileEntity(pos);
            if (te != null) {
                TileChannel tc = (TileChannel) te;
                FluidTank tank = (FluidTank)tc.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, hit.sideHit);
                System.out.println("Tank: " + tank.getFluidAmount());
            }

            if (!world.isRemote && FluidUtil.interactWithFluidHandler(player, hand, world, pos, hit.sideHit)) {
                te.markDirty();
            }
        }

        return true;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileChannel();
    }
}

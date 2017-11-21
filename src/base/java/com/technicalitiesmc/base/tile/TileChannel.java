package com.technicalitiesmc.base.tile;

import com.technicalitiesmc.util.block.TileBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import java.util.ArrayList;
import java.util.List;

public class TileChannel extends TileBase implements ITickable {

    private final FluidTank tank;

    public TileChannel() {
        tank = new FluidTank(1000);
        tank.setTileEntity(this);
    }

    private List<TileChannel> getNeighbours() {
        List<TileChannel> list = new ArrayList<>();
        for (EnumFacing face : EnumFacing.VALUES) {
            TileEntity te = world.getTileEntity(pos.offset(face));
            if (te != null && te instanceof TileChannel) {
                list.add((TileChannel)te);
            }
        }
        return list;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) tank;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void update() {
        if (world.isRemote) return;

        List<TileChannel> neighbours = getNeighbours();
        for (TileChannel channel : neighbours) {
            FluidTank otherTank = (FluidTank)channel.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.DOWN);
            if (otherTank == null) continue;

            float diff = Math.abs(tank.getFluidAmount() - otherTank.getFluidAmount());

            float flow = diff * 0.1f;
            int finalFlow = (int)Math.ceil(Math.max(flow, 5));

            if (tank.getFluidAmount() > otherTank.getFluidAmount()) {
                FluidStack stack = tank.drain(finalFlow, true);
                otherTank.fill(stack, true);
            } else {
                FluidStack stack = otherTank.drain(finalFlow, true);
                tank.fill(stack, true);
            }
        }
    }
}

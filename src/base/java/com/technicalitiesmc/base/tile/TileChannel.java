package com.technicalitiesmc.base.tile;

import com.technicalitiesmc.util.block.TileBase;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TileChannel extends TileBase implements ITickable {

    private final FluidTank tank;

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        NBTBase nbtTag = compound.getTag("tank");
        if (compound.hasKey("tank")) {
            NBTTagCompound nbtTank = (NBTTagCompound) compound.getTag("tank");
            tank.readFromNBT(nbtTank);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);

        NBTTagCompound nbtTank = new NBTTagCompound();
        tank.writeToNBT(nbtTank);
        compound.setTag("tank", nbtTank);

        return compound;
    }

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

            float flow = diff * 0.01f;
            int finalFlow = (int)Math.ceil(Math.min(flow, 5));

            if (tank.getFluidAmount() > otherTank.getFluidAmount()) {
                FluidStack stack = tank.drain(finalFlow, true);
                otherTank.fill(stack, true);
                channel.markDirty();
                markDirty();
            } else {
                FluidStack stack = otherTank.drain(finalFlow, true);
                tank.fill(stack, true);
                channel.markDirty();
                markDirty();
            }
        }
    }

    @Override
    public void writeDescription(PacketBuffer buf) throws IOException {
        super.writeDescription(buf);

        NBTTagCompound nbt = new NBTTagCompound();
        tank.writeToNBT(nbt);
        buf.writeCompoundTag(nbt);
    }

    @Override
    public void readDescription(PacketBuffer buf) throws IOException {
        super.readDescription(buf);

        NBTTagCompound nbt = buf.readCompoundTag();
        tank.readFromNBT(nbt);
    }
}

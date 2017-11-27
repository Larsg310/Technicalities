package com.technicalitiesmc.base.tile;

import com.technicalitiesmc.lib.block.TileBase;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.Map;

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
    @Nonnull
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

    private Map<EnumFacing, TileChannel> getNeighbours() {
        Map<EnumFacing, TileChannel> map = new EnumMap<EnumFacing, TileChannel>(EnumFacing.class);
        for (EnumFacing face : EnumFacing.VALUES) {
            if (face == EnumFacing.UP) continue;
            TileEntity te = world.getTileEntity(pos.offset(face));
            if (te != null && te instanceof TileChannel) {
                map.put(face, (TileChannel)te);
            }
        }
        return map;
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

        Map<EnumFacing, TileChannel> neighbours = getNeighbours();
        for (EnumFacing face : neighbours.keySet()) {
            TileChannel channel = neighbours.get(face);
            if (channel == null) continue;

            FluidTank otherTank = (FluidTank)channel.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face.getOpposite());
            if (otherTank == null) continue;

            float diff = Math.abs(tank.getFluidAmount() - otherTank.getFluidAmount());
            if (face == EnumFacing.DOWN) {
                diff = otherTank.getCapacity() - otherTank.getFluidAmount();
                diff = Math.min(diff, tank.getFluidAmount());
            }

            float flow = diff * 0.01f;

            if (face == EnumFacing.DOWN) {
                flow *= 10;
            }

            int finalFlow = (int)Math.ceil(Math.min(flow, 5));

            if (face == EnumFacing.DOWN || tank.getFluidAmount() > otherTank.getFluidAmount()) {
                FluidStack stack = tank.drain(finalFlow, false);
                if (otherTank.fill(stack, false) == stack.amount) {
                    tank.drain(stack, true);
                    otherTank.fill(stack, true);

                    channel.markDirty();
                    markDirty();
                }
            }
        }
    }

    public void syncTank(){
        NBTTagCompound nbt = new NBTTagCompound();
        tank.writeToNBT(nbt);
        sendPacket(2, nbt);
    }

    @Override
    public void onDataPacket(int id, NBTTagCompound tag) {
        if (id == 2){
            tank.readFromNBT(tag);
        } else {
            super.onDataPacket(id, tag);
        }
    }

    /*

    Please call synctank for syncing tank contents

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
    */
}

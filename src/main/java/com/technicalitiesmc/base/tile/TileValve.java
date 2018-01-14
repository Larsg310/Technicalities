package com.technicalitiesmc.base.tile;

import com.technicalitiesmc.base.tank.MultiblockTank;
import com.technicalitiesmc.lib.block.TileBase;
import com.technicalitiesmc.lib.pool.PooledObject;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;
import java.util.UUID;

public class TileValve extends TileBase {

    private final Valve valve1 = new Valve();
    private final Valve valve2 = new Valve();

    public Valve getValve(EnumFacing.AxisDirection dir) {
        return dir == EnumFacing.AxisDirection.NEGATIVE ? valve1 : valve2;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (valve1.getTank() != null) {
            MultiblockTank.unformTank(valve1);
        }
        if (valve2.getTank() != null) {
            MultiblockTank.unformTank(valve2);
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null)
                || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            return (T) getValve(facing.getAxisDirection());
        }
        return super.getCapability(capability, facing);
    }

    public class Valve implements IFluidHandler {

        private PooledObject<MultiblockTank> tank;

        private Valve() {
        }

        public void setTank(PooledObject<MultiblockTank> tank) {
            this.tank = tank;
        }

        public UUID getTankID() {
            return tank == null ? null : tank.getUUID();
        }

        public MultiblockTank getTank() {
            return tank == null ? null : tank.getOrRequest();
        }

        public void storeTankInfo(NBTTagCompound tag) {
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            MultiblockTank tank = getTank();
            int y = getPos().getY() - getTank().getCorner().getY() - 1;
            return tank.getTankProperties(y);
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if (tank != null) {
                return getTank().fill(resource, !doFill);
            }
            return 0;
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (tank != null) {
                MultiblockTank tank = getTank();
                int y = getPos().getY() - getTank().getCorner().getY();
                return tank.drain(y, resource, resource.amount, !doDrain);
            }
            return null;
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (tank != null) {
                MultiblockTank tank = getTank();
                int y = getPos().getY() - getTank().getCorner().getY();
                return tank.drain(y, null, maxDrain, !doDrain);
            }
            return null;
        }

    }

}

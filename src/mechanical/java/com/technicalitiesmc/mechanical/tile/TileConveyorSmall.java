package com.technicalitiesmc.mechanical.tile;

import com.technicalitiesmc.api.mechanical.conveyor.IConveyorBelt;
import com.technicalitiesmc.mechanical.client.TESRConveyor;
import com.technicalitiesmc.mechanical.conveyor.ConveyorBeltLogic;
import com.technicalitiesmc.mechanical.conveyor.IConveyorBeltHost;
import com.technicalitiesmc.util.block.TileBase;
import com.technicalitiesmc.util.client.SpecialRenderer;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;

@SpecialRenderer(TESRConveyor.class)
public class TileConveyorSmall extends TileBase implements ITickable, IConveyorBeltHost {

    private final ConveyorBeltLogic logic = new ConveyorBeltLogic(this, 9 / 16F);

    @Override
    public void update() {
        logic.tick();
    }

    @Override
    public IConveyorBelt getNeighbor(EnumFacing side) {
        TileEntity te = getWorld().getTileEntity(getPos().offset(side));
        if (te != null && te.hasCapability(IConveyorBelt.CAPABILITY, null)) {
            return te.getCapability(IConveyorBelt.CAPABILITY, null);
        }
        return null;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == IConveyorBelt.CAPABILITY || super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == IConveyorBelt.CAPABILITY) {
            return (T) logic;
        }
        return super.getCapability(capability, facing);
    }

}

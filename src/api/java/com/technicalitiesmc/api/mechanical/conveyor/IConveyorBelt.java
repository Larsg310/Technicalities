package com.technicalitiesmc.api.mechanical.conveyor;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public interface IConveyorBelt {

    @CapabilityInject(IConveyorBelt.class)
    public static final Capability<IConveyorBelt> CAPABILITY = null;

    public float getHeight();

    public EnumFacing getDirection();

    public boolean canInput(EnumFacing side);

    public void insert(EnumFacing side, IConveyorObject object);

}

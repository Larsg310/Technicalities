package com.technicalitiesmc.mechanical.conveyor;

import com.technicalitiesmc.api.mechanical.conveyor.IConveyorBelt;

import net.minecraft.util.EnumFacing;

public interface IConveyorBeltHost {

    public IConveyorBelt getNeighbor(EnumFacing side);

}

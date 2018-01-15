package com.technicalitiesmc.mechanical.conveyor;

import com.technicalitiesmc.api.mechanical.conveyor.IConveyorBelt;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public interface IConveyorBeltHost {
    @Nullable
    public IConveyorBelt getNeighbor(EnumFacing side);

    @Nonnull
    public World getWorld();

    @Nonnull
    public BlockPos getPos();

    @Nonnull
    public EnumFacing.Axis getMovementAxis();

    public float getMovementSpeed();

    @Nonnull
    public default EnumFacing getEjectFacing() {
        return EnumFacing.getFacingFromAxis(
                getMovementSpeed() < 0 ?
                        EnumFacing.AxisDirection.NEGATIVE :
                        EnumFacing.AxisDirection.POSITIVE,
                getMovementAxis());
    }

    // used for client sync
    public default void notifyObjectAdd(UUID id) {}

    // used for client sync
    public default void notifyObjectRemove(UUID id) {}
}
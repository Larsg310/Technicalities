package com.technicalitiesmc.mechanical.conveyor;

import com.technicalitiesmc.api.mechanical.conveyor.IConveyorBelt;
import com.technicalitiesmc.api.mechanical.conveyor.IConveyorObject;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Predicate;

public interface IConveyorBeltHost {
    @Nullable
    public IConveyorBelt getNeighbor(EnumFacing side);

    @Nonnull
    @Deprecated
    public BlockPos getPos();

    public void pickupEntities(Predicate<Entity> op);

    @Nonnull
    public Collection<AxisAlignedBB> getObjectBoundingBoxes(int radius, int height, Predicate<IConveyorObject> filter);

    @Nonnull
    public Collection<AxisAlignedBB> getWorldBoundingBoxes(int radius, int height);

    @Nonnull
    public EnumFacing.Axis getMovementAxis();

    public float getMovementSpeed();

    public void spawnItem(ItemStack stack, Vec3d offset);

    @Nonnull
    public default EnumFacing getEjectFacing() {
        return EnumFacing.getFacingFromAxis(
            getMovementSpeed() < 0 ?
                EnumFacing.AxisDirection.NEGATIVE :
                EnumFacing.AxisDirection.POSITIVE,
            getMovementAxis());
    }

    // used for client sync
    public void notifyObjectAdd(UUID id);

    // used for client sync
    public void notifyObjectRemove(UUID id);
}
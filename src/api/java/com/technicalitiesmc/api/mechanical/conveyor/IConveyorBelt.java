package com.technicalitiesmc.api.mechanical.conveyor;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.Predicate;

public interface IConveyorBelt {
    @CapabilityInject(IConveyorBelt.class)
    public static final Capability<IConveyorBelt> CAPABILITY = null;

    public float getHeight();

    public boolean canInput(EnumFacing side);

    public void insert(EnumFacing side, IConveyorObject object);

    public Collection<AxisAlignedBB> getAllBoundingBoxes(Predicate<IConveyorObject> op);

    @Nonnull
    public EnumFacing.Axis getOrientation();

    @Nonnull
    public Vec3d getMovementVector();

    public void onHostDestroyed();
}

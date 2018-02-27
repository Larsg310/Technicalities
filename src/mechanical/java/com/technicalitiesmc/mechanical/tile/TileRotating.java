package com.technicalitiesmc.mechanical.tile;

import com.technicalitiesmc.lib.block.TileBase;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

public abstract class TileRotating extends TileBase {

    public abstract float getAngle(float partialTicks);

    public float getScale() {
        return 1.0f;
    }

    @Nonnull
    public abstract EnumFacing.Axis getRotationAxis();

    @Nonnull
    public EnumFacing getRotationFacing() {
        return EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, getRotationAxis());
    }

}

package com.technicalitiesmc.mechanical.tile;

import com.technicalitiesmc.lib.block.TileBase;
import net.minecraft.util.EnumFacing;

public abstract class TileRotating extends TileBase {

    public abstract float getAngle(float partialTicks);

    public abstract float getScale();

    public abstract EnumFacing.Axis getRotationAxis();

}

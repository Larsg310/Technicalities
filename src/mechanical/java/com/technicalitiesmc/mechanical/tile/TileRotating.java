package com.technicalitiesmc.mechanical.tile;

import com.technicalitiesmc.lib.block.TileBase;

public abstract class TileRotating extends TileBase {

    public abstract float getAngle(float partialTicks);

    public abstract float getScale();

}

package com.technicalitiesmc.mechanical.tile;

import com.technicalitiesmc.lib.client.SpecialRenderer;
import com.technicalitiesmc.mechanical.client.TESRConveyor;

@SpecialRenderer(TESRConveyor.class)
public class TileConveyor extends TileConveyorBase {
    public TileConveyor() {
        super(1f);
    }
}

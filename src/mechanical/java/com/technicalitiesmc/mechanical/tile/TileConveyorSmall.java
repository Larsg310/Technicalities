package com.technicalitiesmc.mechanical.tile;

import com.technicalitiesmc.lib.client.SpecialRenderer;
import com.technicalitiesmc.mechanical.client.TESRConveyor;

@SpecialRenderer(TESRConveyor.class)
public class TileConveyorSmall extends TileConveyorBase {
    public TileConveyorSmall() {
        super(9 / 16f);
    }
}

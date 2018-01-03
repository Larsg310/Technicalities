package com.technicalitiesmc.base.tile;

import com.technicalitiesmc.api.TechnicalitiesAPI;
import com.technicalitiesmc.lib.block.TileBase;
import elec332.core.api.registration.RegisteredTileEntity;
import net.minecraft.util.ITickable;

/**
 * Created by Elec332 on 3-1-2018.
 */
@RegisteredTileEntity("TKHeatTest")
public class TileHeatTest extends TileBase implements ITickable {

    @Override
    public void update() {
        TechnicalitiesAPI.getHeatHandler(world).addEnergyToBlock(this, pos.up(), 500, 1880.6);
    }

}

package com.technicalitiesmc.util.block;

import elec332.core.tile.TileEntityBase;

public class TileBase extends TileEntityBase {

    public void save() {
        super.markDirty();
    }
    /*

    I don't think this is needed

    @Override
    public void markDirty() {
        super.markDirty();
        //sync();
    }*/

    /**
     * Sends all tile data to the client,
     * only use if need a full sync.
     */
    public void sync() {
        if (getWorld() != null && getPos() != null && !getWorld().isRemote) {
            sendPacket(0, this.getUpdateTag());
        }
    }

}

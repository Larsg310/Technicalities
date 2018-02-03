package com.technicalitiesmc.lib.block;

import elec332.core.tile.TileEntityBase;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

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
    @SuppressWarnings("all")
    public void sync() {
        if (getWorld() != null && getPos() != null && !getWorld().isRemote) {
            syncTile();
        }
    }
}

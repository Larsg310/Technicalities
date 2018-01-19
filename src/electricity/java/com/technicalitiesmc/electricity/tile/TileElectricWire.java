package com.technicalitiesmc.electricity.tile;

import com.technicalitiesmc.lib.block.TileBase;
import net.minecraft.item.EnumDyeColor;

public class TileElectricWire extends TileBase {

    private int colors = 0;

    public boolean hasWire(EnumDyeColor color) {
        return (colors & (1 << color.getMetadata())) != 0;
    }

    public void addWire(EnumDyeColor color) {
        colors |= 1 << color.getMetadata();
    }

    public void removeWire(EnumDyeColor color) {
        colors &= ~(1 << color.getMetadata());
    }

    public int getColorBits() {
        return colors;
    }

}

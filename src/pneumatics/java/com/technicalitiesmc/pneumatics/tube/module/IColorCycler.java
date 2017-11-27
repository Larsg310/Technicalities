package com.technicalitiesmc.pneumatics.tube.module;

import net.minecraft.item.EnumDyeColor;

/**
 * Created by Elec332 on 27-11-2017.
 */
public interface IColorCycler {

    public void cycleColor(int id, boolean backwards);

    public EnumDyeColor getColor(int id);

}

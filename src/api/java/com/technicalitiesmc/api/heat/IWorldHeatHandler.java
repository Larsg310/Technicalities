package com.technicalitiesmc.api.heat;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

/**
 * Created by Elec332 on 27-12-2017.
 */
public interface IWorldHeatHandler {

    @Nullable
    public IHeatObject getHeatObject(BlockPos pos);

    public void addEnergyToBlock(TileEntity tile, double energy, double temp);

    public void addEnergyToSurroundings(TileEntity tile, double energy, double temp, EnumFacing... sides);

}

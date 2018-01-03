package com.technicalitiesmc.api.heat;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

/**
 * Created by Elec332 on 27-12-2017.
 */
public interface IHeatConductor {

   public IThermalMaterial getMaterial();

   public boolean touches(@Nonnull EnumFacing side);

   default public double getM3(IBlockState state){
      return getMaterial().getM3(state);
   }

   default public boolean conductsHeat(IBlockState state){
      return getMaterial().conductsHeat(state);
   }

}

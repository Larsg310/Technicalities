package com.technicalitiesmc.api.heat;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Elec332 on 28-12-2017.
 */
public interface IThermalMaterial extends IForgeRegistryEntry<IThermalMaterial> {

    //  J/(kg*K)
    public double getSpecificHeatCapacity();

    //  W/(m*K)
    public double getThermalConductivity();

    //  kg/m^3
    public double getDensity();

    public double getM3(IBlockState state);

    //  m^2/s
    default public double getThermalDiffusity(){
        return getThermalConductivity() / (getDensity() * getSpecificHeatCapacity());
    }

    /**
     *
     * When false is returned, this material is treated as an insulator;
     * Its temperature will be reset to ambient every tick, and blocks behind it will not get its heat updated
     *
     * @param state
     * @return
     */
    default public boolean conductsHeat(IBlockState state){
        return false;
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryName();

    @Override
    default public IThermalMaterial setRegistryName(ResourceLocation name) {
        throw new UnsupportedOperationException();
    }

    @Override
    default public Class<IThermalMaterial> getRegistryType() {
        return IThermalMaterial.class;
    }

}

package com.technicalitiesmc.lib.util;

import com.google.common.base.Preconditions;
import com.technicalitiesmc.api.heat.IThermalMaterial;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

/**
 * Created by Elec332 on 3-1-2018.
 */
public class DefaultThermalMaterial implements IThermalMaterial {

    public DefaultThermalMaterial(ResourceLocation name, double specificHeatCapacity, double thermalConductivity , double density){
        this(name, specificHeatCapacity, thermalConductivity, density, 1);
    }

    public DefaultThermalMaterial(ResourceLocation name, double specificHeatCapacity, double thermalConductivity , double density, double m3){
        this.name = Preconditions.checkNotNull(name);
        this.specificHeatCapacity = specificHeatCapacity;
        this.thermalConductivity = thermalConductivity;
        this.density = density;
        this.m3 = m3;
        this.conductive = state -> true;
    }

    private final ResourceLocation name;
    private final double specificHeatCapacity, thermalConductivity, density, m3;
    private Predicate<IBlockState> conductive;

    @Override
    public double getSpecificHeatCapacity() {
        return specificHeatCapacity;
    }

    @Override
    public double getThermalConductivity() {
        return thermalConductivity;
    }

    @Override
    public double getDensity() {
        return density;
    }

    @Override
    public double getM3(IBlockState state) {
        return m3;
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryName() {
        return name;
    }

    @Override
    public boolean conductsHeat(IBlockState state) {
        return conductive.test(state);
    }

    public DefaultThermalMaterial cloneWithSize(ResourceLocation name, double m3){
        return new DefaultThermalMaterial(name, specificHeatCapacity, thermalConductivity, density, m3);
    }

    public DefaultThermalMaterial setConductivity(Predicate<IBlockState> predicate){
        this.conductive = Preconditions.checkNotNull(predicate);
        return this;
    }
    public DefaultThermalMaterial setConductivity(final boolean conductivity){
        this.conductive = state -> conductivity;
        return this;
    }


}

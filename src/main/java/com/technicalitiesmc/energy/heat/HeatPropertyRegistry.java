package com.technicalitiesmc.energy.heat;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.technicalitiesmc.api.heat.IHeatPropertyRegistry;
import com.technicalitiesmc.api.heat.IThermalMaterial;
import com.technicalitiesmc.base.Technicalities;
import com.technicalitiesmc.base.init.TKHeatObjects;
import com.technicalitiesmc.lib.util.DefaultThermalMaterial;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Created by Elec332 on 30-12-2017.
 */
public enum HeatPropertyRegistry implements IHeatPropertyRegistry {

    INSTANCE;

    HeatPropertyRegistry(){
        this.registry = Maps.newHashMap();
        this.namez = Maps.newHashMap();
    }

    private final Map<Block, Map<IBlockState, IThermalMaterial>> registry;
    private final Map<ResourceLocation, IThermalMaterial> namez;
    private static final ResourceLocation NULL_MAT_NAME;
    private static final IThermalMaterial NULL_MATERIAL;

    @Override
    public void registerHeatMaterial(IBlockState state, IThermalMaterial material) {
        Block block = state.getBlock();
        Preconditions.checkNotNull(material.getRegistryName());
        Map<IBlockState, IThermalMaterial> subreg = registry.computeIfAbsent(block, m -> Maps.newHashMap());
        if (subreg.get(state) != null || !isValid(material, state)){
            throw new RuntimeException();
        }
        subreg.put(state, material);
        IThermalMaterial now = namez.get(material.getRegistryName());
        if (now == null) {
            namez.put(material.getRegistryName(), material);
        } else if (now != material){
            throw new RuntimeException();
        }
    }

    private boolean isValid(IThermalMaterial material, IBlockState state){
        return material.getThermalConductivity() * material.getM3(state) * 500 < (material.getSpecificHeatCapacity() * material.getDensity() * material.getM3(state));
    }

    @Nonnull
    @Override
    public IThermalMaterial getMaterial(IBlockState state) {
        Map<IBlockState, IThermalMaterial> subreg = registry.get(state.getBlock());
        return subreg == null ? NULL_MATERIAL : subreg.getOrDefault(state, NULL_MATERIAL);
    }

    @Nonnull
    @Override
    public IThermalMaterial getMaterial(ResourceLocation name) {
        return namez.getOrDefault(name, NULL_MATERIAL);
    }

    static {
        NULL_MAT_NAME = new ResourceLocation(Technicalities.MODID, "nullmaterial");
        NULL_MATERIAL = TKHeatObjects.DEFAULT;
    }

}

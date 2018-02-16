package com.technicalitiesmc.base.init;

import com.technicalitiesmc.api.TechnicalitiesAPI;
import com.technicalitiesmc.api.heat.IHeatPropertyRegistry;
import com.technicalitiesmc.base.Technicalities;
import com.technicalitiesmc.lib.util.DefaultThermalMaterial;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

/**
 * Created by Elec332 on 3-1-2018.
 */
public class TKHeatObjects {

    public static final DefaultThermalMaterial IRON, COPPER, GOLD, WATER, AIR, DEFAULT;

    public static void init() {
        IHeatPropertyRegistry registry = TechnicalitiesAPI.heatPropertyRegistry;
        registry.registerHeatMaterial(Blocks.IRON_BLOCK, IRON);
        registry.registerHeatMaterial(Blocks.GOLD_BLOCK, GOLD);
        registry.registerHeatMaterial(Blocks.WATER, WATER);
        registry.registerHeatMaterial(Blocks.AIR, AIR);
    }

    private static DefaultThermalMaterial makeMaterial(String name, double specificHeatCapacity, double thermalConductivity, double density) {
        return new DefaultThermalMaterial(new ResourceLocation(Technicalities.MODID + "heat", name), specificHeatCapacity * 1000, thermalConductivity, density);
    }

    static {
        IRON = makeMaterial("iron", 0.412, 55, 7870).setConductivity(true);
        COPPER = makeMaterial("copper", 0.385, 401, 8940).setConductivity(true);
        GOLD = makeMaterial("gold", 0.129, 318, 19320).setConductivity(true);
        WATER = makeMaterial("water", 4.1813, 0.5818, 1000);
        // Times 300 for airflow (according to wikipedia, heat transfer of air is between 10-100
        int air_factor = 200;
        AIR = makeMaterial("air", 1.012 * air_factor, 0.0209 * air_factor, 1.1839 * air_factor);
        DEFAULT = makeMaterial("default", 4.1813, 0.51, 5515);
    }

}

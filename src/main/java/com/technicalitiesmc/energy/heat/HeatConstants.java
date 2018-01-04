package com.technicalitiesmc.energy.heat;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by Elec332 on 28-12-2017.
 */
public class HeatConstants {

    public static final double THERMAL_SCALAR = 10;

    public static final double ZERO_DEGREES = 273.16;

    public static final double AMBIENT_TEMPERATURE_DEGREES = 21;

    public static final int TEMP_DELTA_REMOVE = 7;

    private static final double MC_TEMP_OFFSET = 10;

    public static int getPowerScalar(){ //debugging THERMAL_SCALAR values
        return 10000;
    }

    public static int getTransferScalar(){ //debugging THERMAL_SCALAR values
        return getPowerScalar() / 10;
    }

    /**
     *
     * Returns the temperature (in K) at the given position.
     * The MC biome "temperature" value is between 0 and 2
     *
     * @param world The world in which the temperature reading should be made
     * @param pos The position at which the temperature reading should be made
     *
     * @return The temperature
     */
    public static double getAmbientTemperature(World world, BlockPos pos){
        return ZERO_DEGREES - MC_TEMP_OFFSET + world.getBiome(pos).getTemperature(pos) * (AMBIENT_TEMPERATURE_DEGREES + MC_TEMP_OFFSET * 2);
    }

}

package com.technicalitiesmc.api.weather;

import net.minecraft.util.math.BlockPos;

/**
 * Created by Elec332 on 16-1-2018.
 */
public interface IWeatherSimulator {

    /**
     * Returns the sun strength (in W/m^2) at the current position.
     * Does not check for obstructions
     *
     * @param pos The position
     * @return Sun strength at the current position
     */
    public double getSunStrength(BlockPos pos);

    /**
     * Returns the wind strength at the current position. (In m/s)
     * Does not check for obstructions.
     *
     * @param pos The position
     * @return Wind strength at the given position.
     */
    public double getWindStrength(BlockPos pos);

}

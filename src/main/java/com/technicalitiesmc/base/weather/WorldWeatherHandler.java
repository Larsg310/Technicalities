package com.technicalitiesmc.base.weather;

import com.technicalitiesmc.api.weather.IWeatherSimulator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

/**
 * Created by Elec332 on 16-1-2018.
 */
public class WorldWeatherHandler implements IWeatherSimulator {

    protected WorldWeatherHandler(World world){
        this.world = world;
    }

    private final World world;
    private static final double AVG_PEAK_SUN_ENERGY = 1366;

    /**
     * Returns the sun strength (in W/m^2) at the current position.
     * Does not check for obstructions
     *
     * @param pos The position
     * @return Sun strength at the current position
     */
    public double getSunStrength(BlockPos pos){
        return world.getSkylightSubtracted() * (AVG_PEAK_SUN_ENERGY / 16) * getSunModifiers(pos);
    }

    private double getSunModifiers(BlockPos pos){
        Biome biome = world.getBiome(pos);
        double modifier = 1.0;
        switch (biome.getTempCategory()){
            case COLD:
                modifier *= 0.87;
                break;
            case WARM:
                modifier *= 1.19;
                break;
        }
        if (biome.isHighHumidity()){
            modifier *= 0.95;
        }
        if (world.isRainingAt(pos)){
            modifier *= 0.6;
            if (world.isThundering()){
                modifier *= 0.78;
            }
        }

        return modifier;
    }

    /**
     * Returns the wind strength at the current position. (In KM/hr)
     * Does not check for obstructions.
     *
     * @param pos The position
     * @return Wind strength at the given position.
     */
    public double getWindStrength(BlockPos pos){
        //TODO: Some random wind gen stuff with perlin noise
        double speed = 1;
        if (world.isRainingAt(pos)){
            speed = 7.6;
            if (world.isThundering()){
                speed *= 2;
            }
        }
        speed *= getWindModifier(pos);
        return speed;
    }

    private double getWindModifier(BlockPos pos){
        Biome biome = world.getBiome(pos);
        double modifier = 1.0;
        if (pos.getY() < 54){
            modifier *= 0.05;
        }
        modifier *= Math.sqrt(1.2-4*biome.getHeightVariation());

        return modifier;
    }

}

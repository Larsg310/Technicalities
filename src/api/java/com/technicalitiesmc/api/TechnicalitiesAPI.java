package com.technicalitiesmc.api;

import com.google.common.base.Preconditions;
import com.technicalitiesmc.api.electricity.IEnergyObject;
import com.technicalitiesmc.api.heat.IHeatConductor;
import com.technicalitiesmc.api.heat.IHeatPropertyRegistry;
import com.technicalitiesmc.api.heat.IWorldHeatHandler;
import com.technicalitiesmc.api.weather.IWeatherSimulator;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import javax.annotation.Nonnull;

/**
 * Created by Elec332 on 23-11-2017.
 */
public class TechnicalitiesAPI {

	public static final IHeatPropertyRegistry heatPropertyRegistry;

	@CapabilityInject(IEnergyObject.class)
	public static final Capability<IEnergyObject> ELECTRICITY_CAP;

	@CapabilityInject(IHeatConductor.class)
	public static final Capability<IHeatConductor> HEAT_CONDUCTOR_CAP;

	//World capability's
	@CapabilityInject(IWorldHeatHandler.class)
	public static final Capability<IWorldHeatHandler> WORLD_HEAT_CAP;

	@CapabilityInject(IWeatherSimulator.class)
	public static final Capability<IWeatherSimulator> WORLD_WEATHER_CAP;


	@Nonnull
	@SuppressWarnings("all")
	public static IWorldHeatHandler getHeatHandler(@Nonnull World world){
		return Preconditions.checkNotNull(world.getCapability(WORLD_HEAT_CAP, null));
	}

	@Nonnull
	@SuppressWarnings("all")
	public static IWeatherSimulator getWeatherSimulator(@Nonnull World world){
		return Preconditions.checkNotNull(world.getCapability(WORLD_WEATHER_CAP, null));
	}

	static {
		heatPropertyRegistry = null;
		ELECTRICITY_CAP = null;
		WORLD_HEAT_CAP = null;
		HEAT_CONDUCTOR_CAP = null;
		WORLD_WEATHER_CAP = null;
	}

}

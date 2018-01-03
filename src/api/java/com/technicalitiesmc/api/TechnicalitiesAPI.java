package com.technicalitiesmc.api;

import com.technicalitiesmc.api.electricity.IEnergyObject;
import com.technicalitiesmc.api.heat.IHeatConductor;
import com.technicalitiesmc.api.heat.IHeatPropertyRegistry;
import com.technicalitiesmc.api.heat.IWorldHeatHandler;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

/**
 * Created by Elec332 on 23-11-2017.
 */
public class TechnicalitiesAPI {

	public static final IHeatPropertyRegistry heatPropertyRegistry;

	@CapabilityInject(IEnergyObject.class)
	public static final Capability<IEnergyObject> ELECTRICITY_CAP;

	//World capability
	@CapabilityInject(IWorldHeatHandler.class)
	public static final Capability<IWorldHeatHandler> WORLD_HEAT_CAP;

	@CapabilityInject(IHeatConductor.class)
	public static final Capability<IHeatConductor> HEAT_CONDUCTOR_CAP;

	static {
		heatPropertyRegistry = null;
		ELECTRICITY_CAP = null;
		WORLD_HEAT_CAP = null;
		HEAT_CONDUCTOR_CAP = null;
	}

}

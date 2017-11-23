package com.technicalitiesmc.api;

import com.technicalitiesmc.api.electricity.IEnergyObject;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

/**
 * Created by Elec332 on 23-11-2017.
 */
public class TechnicalitiesAPI {



	@CapabilityInject(IEnergyObject.class)
	public static final Capability<IEnergyObject> ELECTRICITY_CAP = null;

}

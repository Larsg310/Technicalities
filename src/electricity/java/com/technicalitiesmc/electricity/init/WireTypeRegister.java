package com.technicalitiesmc.electricity.init;

import com.technicalitiesmc.api.electricity.IWireType;
import com.technicalitiesmc.electricity.util.EnumWireType;
import elec332.core.api.registration.IObjectRegister;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Created by Elec332 on 23-11-2017.
 */
public class WireTypeRegister implements IObjectRegister<IWireType> {

	@Override
	public void register(IForgeRegistry<IWireType> registry) {
		registry.registerAll(EnumWireType.values());
	}

	@Override
	public Class<IWireType> getType() {
		return IWireType.class;
	}

}

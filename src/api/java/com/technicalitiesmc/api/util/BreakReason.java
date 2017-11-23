package com.technicalitiesmc.api.util;

import net.minecraftforge.common.util.EnumHelper;

/**
 * Created by Elec332 on 7-11-2017.
 */
public enum BreakReason {

	OVERPOWERED_GENERATOR,
	SHORT_CIRCUIT;

	public static void registerBreakReason(String name){
		String enumName = name.toUpperCase();
		EnumHelper.addEnum(BreakReason.class, enumName, new Class[0]);
	}

}

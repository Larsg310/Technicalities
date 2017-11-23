package com.technicalitiesmc.electricity.util;

import elec332.core.config.Configurable;

/**
 * Created by Elec332 on 23-11-2017.
 */
public class Config {

	@Configurable(comment = "Sets whether the rebuilding of a circuit will be done in a different thread, will increase performance on multicore CPU's.")
	public static boolean threadedCircuitCompilation = true;

}

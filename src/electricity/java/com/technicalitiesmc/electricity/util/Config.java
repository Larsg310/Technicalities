package com.technicalitiesmc.electricity.util;

import elec332.core.config.Configurable;
import net.minecraftforge.common.config.Configuration;

/**
 * Created by Elec332 on 23-11-2017.
 */
public class Config {

	@Configurable(comment = "Sets whether the rebuilding of a circuit will be done in a different thread, will increase performance on multicore CPU's.")
	public static boolean threadedCircuitCompilation = true;

	@Configurable(category = Configuration.CATEGORY_CLIENT, comment = "Use non-dynamic rendering when the wire contains only one color.")
	public static boolean singleWirePNGRendering = true;

}

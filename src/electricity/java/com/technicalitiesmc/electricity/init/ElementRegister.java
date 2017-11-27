package com.technicalitiesmc.electricity.init;

import com.technicalitiesmc.electricity.simulation.WireElement;
import com.technicalitiesmc.electricity.util.Wire;
import com.technicalitiesmc.energy.electricity.simulation.CircuitElementFactory;

/**
 * Created by Elec332 on 23-11-2017.
 */
public class ElementRegister {

	public static void init(){
		CircuitElementFactory.INSTANCE.registerComponentWrapper(Wire.class, WireElement.class, WireElement::new);
	}

}

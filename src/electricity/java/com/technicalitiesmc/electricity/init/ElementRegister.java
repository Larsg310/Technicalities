package com.technicalitiesmc.electricity.init;

import com.technicalitiesmc.electricity.simulation.CircuitElementFactory;
import com.technicalitiesmc.electricity.simulation.WireElement;
import com.technicalitiesmc.electricity.util.Wire;

/**
 * Created by Elec332 on 23-11-2017.
 */
public class ElementRegister {

	public static void init(){
		CircuitElementFactory.INSTANCE.registerComponentWrapper(Wire.class, WireElement.class, WireElement::new);
	}

}

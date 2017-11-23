package com.technicalitiesmc.electricity.simulation;

import com.google.common.collect.Multimap;
import com.technicalitiesmc.api.electricity.component.CircuitElement;
import com.technicalitiesmc.api.util.ConnectionPoint;

import java.util.List;

/**
 * Created by Elec332 on 14-11-2017.
 */
public interface ICircuitCompressor {

	public Multimap<CompressedCircuitElement, CircuitElement> compress(List<CircuitElement> elements, Multimap<ConnectionPoint, CircuitElement<?>> map2);

}

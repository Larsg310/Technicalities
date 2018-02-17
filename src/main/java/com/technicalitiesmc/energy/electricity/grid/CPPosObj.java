package com.technicalitiesmc.energy.electricity.grid;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.technicalitiesmc.api.electricity.component.CircuitElement;
import com.technicalitiesmc.api.util.ConnectionPoint;

/**
 * Created by Elec332 on 18-11-2017.
 */
class CPPosObj {

    Multimap<ConnectionPoint, CircuitElement> connections = HashMultimap.create();

}

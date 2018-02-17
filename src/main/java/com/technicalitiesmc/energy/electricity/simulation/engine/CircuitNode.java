package com.technicalitiesmc.energy.electricity.simulation.engine;

import com.technicalitiesmc.api.util.ConnectionPoint;

import java.util.Vector;

final class CircuitNode {

    ConnectionPoint cp;
    Vector<CircuitNodeLink> links;
    boolean internal;

    CircuitNode() {
        links = new Vector<>();
    }

}

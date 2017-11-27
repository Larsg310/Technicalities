package com.technicalitiesmc.energy.electricity.simulation.engine;

import com.technicalitiesmc.api.util.ConnectionPoint;

import java.util.Vector;

public final class CircuitNode {

	CircuitNode() {
		links = new Vector<>();
	}

	ConnectionPoint cp;
	Vector<CircuitNodeLink> links;
	boolean internal;

}

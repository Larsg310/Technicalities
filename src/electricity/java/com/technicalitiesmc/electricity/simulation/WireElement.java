package com.technicalitiesmc.electricity.simulation;

import com.technicalitiesmc.api.electricity.IEnergyReceiver;
import com.technicalitiesmc.electricity.util.Wire;
import com.technicalitiesmc.energy.electricity.simulation.AbstractResistorElement;

import javax.annotation.Nullable;

/**
 * Created by Elec332 on 13-11-2017.
 */
public class WireElement extends AbstractResistorElement<Wire> {

	public WireElement(Wire energyTile) {
		super(energyTile);
	}

	@Override
	public double getResistance() {
		return 0.000001;
	}

	@Nullable
	@Override
	protected IEnergyReceiver getReceiver() {
		return null;
	}

	@Override
	public void apply() {


	}

	@Override
	public boolean isWire() {
		return super.isWire();
	}

	/*@Override
	public void stamp() {
		getCircuit().stampVoltageSource(nodes[0], nodes[1], voltSource, 0);
		getCircuit().stampResistor(nodes[0], nodes[1], 1);
	}

	@Override
	public int getVoltageSourceCount() {
		return 1;
	}

	@Override
	protected double getPower() {
		return 0;
	}

	@Override
	protected double getVoltageDiff() {
		return volts[0];
	}

	@Override
	public boolean isWire() {
		return true;
	}*/

}

package com.technicalitiesmc.energy.electricity.simulation;

import com.technicalitiesmc.api.electricity.IEnergySource;
import com.technicalitiesmc.api.electricity.component.CircuitElement;

/**
 * Created by Elec332 on 12-11-2017.
 */
public class VoltageElement extends CircuitElement<IEnergySource> {

    public VoltageElement(IEnergySource energySource) {
        super(energySource);
    }

    @Override
    public void stamp() {
        getCircuit().stampVoltageSource(nodes[0], nodes[1], voltSource);
        //getCircuit().stampVoltageSource(nodes[0], nodes[1], voltSource, getVoltage());
    }

    @Override
    public void doStep() {
        getCircuit().updateVoltageSource(nodes[0], nodes[1], voltSource, getVoltage());
    }

    private double getVoltage() {
        return energyTile.getCurrentAverageEF();
    }

    @Override
    public int getVoltageSourceCount() {
        return 1;
    }

}

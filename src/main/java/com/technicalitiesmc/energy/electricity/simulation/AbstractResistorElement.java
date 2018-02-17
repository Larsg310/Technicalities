package com.technicalitiesmc.energy.electricity.simulation;

import com.technicalitiesmc.api.electricity.IEnergyObject;
import com.technicalitiesmc.api.electricity.IEnergyReceiver;
import com.technicalitiesmc.api.electricity.component.CircuitElement;

import javax.annotation.Nullable;

/**
 * Created by Elec332 on 12-11-2017.
 */
public abstract class AbstractResistorElement<T extends IEnergyObject> extends CircuitElement<T> {

    public AbstractResistorElement(T receiver) {
        super(receiver);
    }


    public abstract double getResistance();

    @Nullable
    protected abstract IEnergyReceiver getReceiver();

    @Override
    protected void calculateCurrent() {
        current = getVoltageDiff() / getResistance();
    }

    @Override
    public void stamp() {
        getCircuit().stampResistor(nodes[0], nodes[1], getResistance());
    }

    @Override
    public abstract void apply();

}

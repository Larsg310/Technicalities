package com.technicalitiesmc.electricity.simulation;

import com.technicalitiesmc.api.electricity.IEnergyObject;

/**
 * Created by Elec332 on 5-1-2018.
 */
public interface IElectricityTransformer extends IEnergyObject {

    public double getInductance();

    public double getRatio();

}

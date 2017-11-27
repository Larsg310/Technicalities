package com.technicalitiesmc.base;

import elec332.core.config.Configurable;

/**
 * Created by Elec332 on 27-11-2017.
 */
public class TechnicalitiesConfig {

    @Configurable.Class
    public static class Debug {

        @Configurable
        public static boolean extremeElectricityGridDebug = false;

    }

}

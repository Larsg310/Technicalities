package com.technicalitiesmc.pneumatics.init;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.api.pneumatics.TubeModule;
import com.technicalitiesmc.pneumatics.tube.module.TMColorFilter;
import com.technicalitiesmc.pneumatics.tube.module.TMFilter;
import com.technicalitiesmc.pneumatics.tube.module.TMMembrane;
import com.technicalitiesmc.pneumatics.tube.module.TMPlug;
import com.technicalitiesmc.pneumatics.tube.module.TMSorter;

public class TKTubeModules {

    public static TubeModule.Type<TMColorFilter> color_filter;
    public static TubeModule.Type<TMMembrane> membrane;
    public static TubeModule.Type<TMPlug> plug;
    public static TubeModule.Type<TMFilter> filter;
    public static TubeModule.Type<TMSorter> sorter;

    public static void initialize() {
        color_filter = new TMColorFilter.Type();
        membrane = new TMMembrane.Type();
        plug = new TMPlug.Type();
        filter = new TMFilter.Type();
        sorter = new TMSorter.Type();
    }

    public static void register() {
        Technicalities.register(color_filter.setRegistryName("color_filter"));
        Technicalities.register(membrane.setRegistryName("membrane"));
        Technicalities.register(plug.setRegistryName("plug"));
        Technicalities.register(filter.setRegistryName("filter"));
        Technicalities.register(sorter.setRegistryName("sorter"));
    }

}

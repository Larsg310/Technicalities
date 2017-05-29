package com.technicalitiesmc;

import com.technicalitiesmc.lib.module.IModule;

public interface ITKModule extends IModule {

    public void preInit();

    public void init();

    public void postInit();

}

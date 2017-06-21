package com.technicalitiesmc.base;

import com.technicalitiesmc.ITKModule;
import com.technicalitiesmc.ModuleProxy;
import com.technicalitiesmc.TKModule;
import com.technicalitiesmc.base.init.TKBaseBlocks;
import com.technicalitiesmc.base.init.TKBaseResources;

@TKModule(value = "base", canBeDisabled = false)
public class TKBase implements ITKModule {

    @ModuleProxy(module = "base", serverSide = "com.technicalitiesmc.base.TKBaseCommonProxy",
            clientSide = "com.technicalitiesmc.base.client.TKBaseClientProxy")
    public static TKBaseCommonProxy proxy;

    @Override
    public void preInit() {
        // Initialize and register blocks
        TKBaseBlocks.initialize();
        TKBaseBlocks.register();

        // Initialize and register resources
        TKBaseResources.initialize();
        TKBaseResources.register();
    }

    @Override
    public void init() {

    }

    @Override
    public void postInit() {

    }

}

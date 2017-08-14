package com.technicalitiesmc.base;

import com.technicalitiesmc.ITKModule;
import com.technicalitiesmc.ModuleProxy;
import com.technicalitiesmc.TKModule;
import com.technicalitiesmc.base.event.OreEventHandler;
import com.technicalitiesmc.base.init.TKBaseBlocks;
import com.technicalitiesmc.base.init.TKBaseItems;
import com.technicalitiesmc.base.init.TKBaseResources;

import net.minecraftforge.common.MinecraftForge;

@TKModule(value = "base", canBeDisabled = false)
public class TKBase implements ITKModule {

    @ModuleProxy(module = "base", serverSide = "com.technicalitiesmc.base.TKBaseCommonProxy",
            clientSide = "com.technicalitiesmc.base.client.TKBaseClientProxy")
    public static TKBaseCommonProxy proxy;

    @Override
    public void preInit() {
        // Pre-init the proxy
        proxy.preInit();

        // Initialize and register blocks
        TKBaseBlocks.initialize();
        TKBaseBlocks.register();

        // Initialize and register items
        TKBaseItems.initialize();
        TKBaseItems.register();

        // Initialize and register resources
        TKBaseResources.initialize();
        TKBaseResources.register();

        // Register event handlers
        MinecraftForge.EVENT_BUS.register(new OreEventHandler());
    }

    @Override
    public void init() {

    }

    @Override
    public void postInit() {

    }

}

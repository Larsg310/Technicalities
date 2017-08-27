package com.technicalitiesmc.mechanical;

import com.technicalitiesmc.ITKModule;
import com.technicalitiesmc.TKModule;
import com.technicalitiesmc.mechanical.init.TKMechanicalBlocks;
import com.technicalitiesmc.mechanical.kinesis.KineticManager;

import net.minecraftforge.common.MinecraftForge;

@TKModule("mechanical")
public class TKMechanical implements ITKModule {

    @Override
    public void preInit() {
        // Initialize and register blocks
        TKMechanicalBlocks.initialize();
        TKMechanicalBlocks.register();
    }

    @Override
    public void init() {
        MinecraftForge.EVENT_BUS.register(KineticManager.INSTANCE);
    }

    @Override
    public void postInit() {

    }

}

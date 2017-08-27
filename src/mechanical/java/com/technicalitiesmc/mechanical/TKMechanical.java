package com.technicalitiesmc.mechanical;

import com.technicalitiesmc.ITKModule;
import com.technicalitiesmc.TKModule;
import com.technicalitiesmc.api.mechanical.IGearAttachable;
import com.technicalitiesmc.api.mechanical.IShaftAttachable;
import com.technicalitiesmc.mechanical.init.TKMechanicalBlocks;
import com.technicalitiesmc.mechanical.kinesis.KineticManager;
import com.technicalitiesmc.util.simple.SimpleCapability;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

@TKModule("mechanical")
public class TKMechanical implements ITKModule {

    // Used to register simple capabilities
    @SimpleCapability
    @CapabilityInject(IShaftAttachable.class)
    public static Capability<IShaftAttachable> CAP_SHAFT_ATTACHABLE;
    @SimpleCapability
    @CapabilityInject(IGearAttachable.class)
    public static Capability<IGearAttachable> CAP_GEAR_ATTACHABLE;

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

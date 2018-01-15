package com.technicalitiesmc.mechanical;

import com.technicalitiesmc.api.mechanical.IGearAttachable;
import com.technicalitiesmc.api.mechanical.IShaftAttachable;
import com.technicalitiesmc.api.mechanical.conveyor.IConveyorBelt;
import com.technicalitiesmc.base.Technicalities;
import com.technicalitiesmc.lib.simple.SimpleCapability;
import com.technicalitiesmc.mechanical.kinesis.KineticManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = TKMechanical.MODID, name = TKMechanical.NAME, version = TKMechanical.VERSION,
        dependencies = "required-after:" + Technicalities.MODID)
public class TKMechanical {
    public static final String MODID = "tkmechanical", NAME = "Technicalities Mechanical", VERSION = "%VERSION%";

    @SimpleCapability
    @CapabilityInject(IConveyorBelt.class)
    public static final Capability<IConveyorBelt> CAPABILITY = null;

    // Used to register simple capabilities
    @SimpleCapability
    @CapabilityInject(IShaftAttachable.class)
    public static Capability<IShaftAttachable> CAP_SHAFT_ATTACHABLE;
    @SimpleCapability
    @CapabilityInject(IGearAttachable.class)
    public static Capability<IGearAttachable> CAP_GEAR_ATTACHABLE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(KineticManager.INSTANCE);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }
}

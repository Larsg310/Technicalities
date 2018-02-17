package com.technicalitiesmc.mechanical;

import com.technicalitiesmc.base.Technicalities;
import com.technicalitiesmc.mechanical.proxy.TKMCommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = TKMechanical.MODID, name = TKMechanical.NAME, version = TKMechanical.VERSION,
    dependencies = "required-after:" + Technicalities.MODID)
public class TKMechanical {
    public static final String MODID = "tkmechanical", NAME = "Technicalities Mechanical", VERSION = "%VERSION%";

    @SidedProxy(serverSide = "com.technicalitiesmc.mechanical.proxy.TKMCommonProxy", clientSide = "com.technicalitiesmc.mechanical.proxy.TKMClientProxy")
    public static TKMCommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}

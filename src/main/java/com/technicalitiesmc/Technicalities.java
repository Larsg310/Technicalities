package com.technicalitiesmc;

import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Technicalities.MODID, name = Technicalities.NAME, version = Technicalities.VERSION, dependencies = "required-after:tklib")
public class Technicalities {

    public static final String MODID = "technicalities", NAME = "Technicalities", VERSION = "%VERSION%";

    public static Logger log;

    private TKModuleManager modules;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Initialize log
        log = event.getModLog();

        // Load submodules and log them
        modules = new TKModuleManager();
        modules.initProxies();
        modules.save();
        log.info("Loaded " + modules.getModules().size() + " submodules: "
                + modules.getModules().stream().map(TKModuleManager::getName).collect(Collectors.toList()));

        // Pre-initialize modules
        modules.forEach(ITKModule::preInit);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        // Initialize modules
        modules.forEach(ITKModule::init);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        // Post-initialize modules
        modules.forEach(ITKModule::postInit);
    }

}

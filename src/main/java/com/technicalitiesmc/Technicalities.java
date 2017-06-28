package com.technicalitiesmc;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.technicalitiesmc.lib.resource.ResourceManager;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

@Mod(modid = Technicalities.MODID, name = Technicalities.NAME, version = Technicalities.VERSION, dependencies = "required-after:tklib")
public class Technicalities {

    public static final String MODID = "technicalities", NAME = "Technicalities", VERSION = "%VERSION%";

    public static Logger log;

    private TKModuleManager modules;

    public Technicalities() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Initialize log
        log = event.getModLog();

        // Load submodules and log them
        modules = new TKModuleManager();
        modules.initProxies();
        modules.save();
        modules.forEach(ITKModule::initRegistries);
        log.info("Loaded " + modules.getModules().size() + " submodules: "
                + modules.getModules().stream().map(TKModuleManager::getName).collect(Collectors.toList()));

        // Pre-initialize modules
        modules.forEach(ITKModule::preInit);

        // Complete resource registration
        ResourceManager.INSTANCE.completeRegistration(Technicalities::register);
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

    private static Multimap<Class<?>, Pair<IForgeRegistryEntry<?>, List<Runnable>>> registryObjects = MultimapBuilder.hashKeys()
            .arrayListValues().build();

    public static Consumer<Runnable> register(IForgeRegistryEntry<?> object) {
        List<Runnable> list = new ArrayList<>();
        registryObjects.put(object.getRegistryType(), Pair.of(object, list));
        return list::add;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @SubscribeEvent
    public void onRegistryEvent(RegistryEvent.Register event) {
        IForgeRegistry reg = event.getRegistry();
        for (Pair<IForgeRegistryEntry<?>, List<Runnable>> pair : registryObjects.get(reg.getRegistrySuperType())) {
            reg.register(pair.getKey());
            pair.getValue().forEach(Runnable::run);
        }
    }

}

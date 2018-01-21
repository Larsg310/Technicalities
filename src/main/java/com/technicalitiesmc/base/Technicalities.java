package com.technicalitiesmc.base;

import com.technicalitiesmc.api.TechnicalitiesAPI;
import com.technicalitiesmc.api.heat.IHeatConductor;
import com.technicalitiesmc.api.heat.IWorldHeatHandler;
import com.technicalitiesmc.base.event.OreEventHandler;
import com.technicalitiesmc.base.init.TKHeatObjects;
import com.technicalitiesmc.base.network.PacketGuiButton;
import com.technicalitiesmc.base.proxies.TKCommonProxy;
import com.technicalitiesmc.base.weather.WeatherHandler;
import com.technicalitiesmc.energy.electricity.grid.ElectricityGridHandler;
import com.technicalitiesmc.energy.heat.HeatPropertyRegistry;
import com.technicalitiesmc.energy.heat.WorldHeatHandler;
import com.technicalitiesmc.lib.simple.SimpleCapabilityManager;
import com.technicalitiesmc.lib.simple.SimpleRegistryManager;
import com.technicalitiesmc.lib.util.DefaultCapabilityProvider;
import elec332.core.api.network.INetworkHandler;
import elec332.core.api.network.ModNetworkHandler;
import elec332.core.inventory.window.WindowManager;
import elec332.core.java.ReflectionHelper;
import elec332.core.main.ElecCore;
import elec332.core.main.ElecCoreRegistrar;
import elec332.core.util.RegistryHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod.EventBusSubscriber
@Mod(modid = Technicalities.MODID, name = Technicalities.NAME, version = Technicalities.VERSION, dependencies = "required-after:" + ElecCore.MODID)
public class Technicalities {

    public static final String MODID = "technicalities", NAME = "Technicalities", VERSION = "%VERSION%";

    @SidedProxy(serverSide = "com.technicalitiesmc.base.proxies.TKCommonProxy", clientSide = "com.technicalitiesmc.base.proxies.TKClientProxy")
    public static TKCommonProxy proxy;

    public static Logger log;
    public static File baseFolder; //All config files of submods can go in here

    @ModNetworkHandler
    public static INetworkHandler networkHandler;

    public static ElectricityGridHandler electricityGridHandler;

    private ASMDataTable asmTable;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Initialize log and load ASM table
        log = LogManager.getLogger(NAME);
        asmTable = event.getAsmData();

        baseFolder = new File(event.getModConfigurationDirectory(), MODID);
        ElecCoreRegistrar.GRIDHANDLERS.register(electricityGridHandler = new ElectricityGridHandler());
        WindowManager.INSTANCE.register(proxy);

        // Init capabilities
        SimpleCapabilityManager.INSTANCE.init(asmTable);
        SimpleRegistryManager.INSTANCE.init(asmTable);

        MinecraftForge.EVENT_BUS.register(new OreEventHandler());
        try {
            ReflectionHelper.makeFinalFieldModifiable(TechnicalitiesAPI.class.getDeclaredField("heatPropertyRegistry")).set(null, HeatPropertyRegistry.INSTANCE);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        RegistryHelper.registerEmptyCapability(IHeatConductor.class);
        RegistryHelper.registerEmptyCapability(IWorldHeatHandler.class);
        DefaultCapabilityProvider.registerWorldCapabilityProvider(new ResourceLocation(Technicalities.MODID, "heatapi"), TechnicalitiesAPI.WORLD_HEAT_CAP, world -> new WorldHeatHandler());
        WeatherHandler.preInit();
        TKHeatObjects.init();

        // Register packets
        networkHandler.registerPacket(PacketGuiButton.class, Side.SERVER);
        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Register TESRs
        proxy.bindSpecialRenderers(asmTable);

    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }

}

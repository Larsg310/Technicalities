package com.technicalitiesmc;

import com.technicalitiesmc.base.TKBase;
import com.technicalitiesmc.electricity.grid.ElectricityGridHandler;
import com.technicalitiesmc.util.block.TileBase;
import com.technicalitiesmc.util.network.GuiHandler;
import com.technicalitiesmc.util.network.NetworkHandler;
import com.technicalitiesmc.util.network.PacketTileUpdate;
import com.technicalitiesmc.util.simple.SimpleCapabilityManager;
import com.technicalitiesmc.util.simple.SimpleRegistryManager;
import elec332.core.main.ElecCoreRegistrar;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod.EventBusSubscriber
@Mod(modid = Technicalities.MODID, name = Technicalities.NAME, version = Technicalities.VERSION)
public class Technicalities {

    public static final String MODID = "technicalities", NAME = "Technicalities", VERSION = "%VERSION%";

    @SidedProxy(serverSide = "com.technicalitiesmc.TKCommonProxy", clientSide = "com.technicalitiesmc.TKClientProxy")
    public static TKCommonProxy proxy;

    public static Logger log;
    public static File baseFolder; //All config files of submods can go in here

    public static final NetworkHandler networkHandler = new NetworkHandler(MODID);
    public static final GuiHandler guiHandler = new GuiHandler();

    public static ElectricityGridHandler electricityGridHandler;

    private ASMDataTable asmTable;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Initialize log and load ASM table
        log = LogManager.getLogger(NAME);
        asmTable = event.getAsmData();

        baseFolder = new File(event.getModConfigurationDirectory(), MODID);
        ElecCoreRegistrar.GRIDHANDLERS.register(electricityGridHandler = new ElectricityGridHandler());

        // Init capabilities
        SimpleCapabilityManager.INSTANCE.init(asmTable);
        SimpleRegistryManager.INSTANCE.init(asmTable);

        // Register packets
        networkHandler.registerPacket(PacketTileUpdate.class, Side.CLIENT);

        // Preinit TKBase
        TKBase.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Register TESRs
        proxy.bindSpecialRenderers(asmTable);

        // Register GUI Handler
        NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler);

        // Init TKBase
        TKBase.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        // Postinit TKBase
        TKBase.postInit(event);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            TileBase.sendSyncPackets();
        }
    }

}

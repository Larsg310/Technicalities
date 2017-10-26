package com.technicalitiesmc.base;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.base.event.OreEventHandler;
import com.technicalitiesmc.base.network.PacketGuiButton;
import com.technicalitiesmc.util.network.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

public class TKBase {

    @SidedProxy(modId = Technicalities.MODID, serverSide = "com.technicalitiesmc.base.TKBaseCommonProxy",
            clientSide = "com.technicalitiesmc.base.client.TKBaseClientProxy")
    public static TKBaseCommonProxy proxy;

    public static NetworkHandler NETWORK_HANDLER = new NetworkHandler(Technicalities.MODID + ".base");

    public static void preInit(FMLPreInitializationEvent event) {
        // Pre-init the proxy
        proxy.preInit();

        // Register event handlers
        MinecraftForge.EVENT_BUS.register(new OreEventHandler());

        // Register packets
        NETWORK_HANDLER.registerPacket(PacketGuiButton.class, Side.SERVER);
    }

    public static void init(FMLInitializationEvent event) {
    }

    public static void postInit(FMLPostInitializationEvent event) {
    }

}

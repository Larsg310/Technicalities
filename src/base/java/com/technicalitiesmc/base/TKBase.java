package com.technicalitiesmc.base;

import com.technicalitiesmc.ITKModule;
import com.technicalitiesmc.ModuleProxy;
import com.technicalitiesmc.TKModule;
import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.base.event.OreEventHandler;
import com.technicalitiesmc.base.init.TKBaseBlocks;
import com.technicalitiesmc.base.init.TKBaseItems;
import com.technicalitiesmc.base.init.TKBaseResources;
import com.technicalitiesmc.base.network.PacketGuiButton;
import com.technicalitiesmc.lib.network.NetworkHandler;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;

@TKModule(value = "base", canBeDisabled = false)
public class TKBase implements ITKModule {

    @ModuleProxy(module = "base", serverSide = "com.technicalitiesmc.base.TKBaseCommonProxy",
            clientSide = "com.technicalitiesmc.base.client.TKBaseClientProxy")
    public static TKBaseCommonProxy proxy;

    public static NetworkHandler NETWORK_HANDLER = new NetworkHandler(Technicalities.MODID + ".base");

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

        // Register packets
        NETWORK_HANDLER.registerPacket(PacketGuiButton.class, Side.SERVER);
    }

    @Override
    public void init() {

    }

    @Override
    public void postInit() {

    }

}

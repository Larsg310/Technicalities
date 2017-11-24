package com.technicalitiesmc.pneumatics;

import com.google.common.base.Throwables;
import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.api.pneumatics.TubeModule;
import com.technicalitiesmc.pneumatics.init.TKPneumaticsBlocks;
import com.technicalitiesmc.pneumatics.network.*;
import com.technicalitiesmc.pneumatics.tube.TubeTicker;
import elec332.core.api.network.INetworkHandler;
import elec332.core.api.network.ModNetworkHandler;
import elec332.core.java.ReflectionHelper;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mod(modid = TKPneumatics.MODID, name = TKPneumatics.NAME, version = TKPneumatics.VERSION,
        dependencies = "required-after:" + Technicalities.MODID)
public class TKPneumatics {

    public static final String MODID = "tkpneumatics", NAME = "Technicalities Pneumatics", VERSION = "%VERSION%";

    @SidedProxy(serverSide = "com.technicalitiesmc.pneumatics.TKPCommonProxy",
            clientSide = "com.technicalitiesmc.pneumatics.client.TKPClientProxy")
    public static TKPCommonProxy proxy;

    @ModNetworkHandler
    public static INetworkHandler networkHandler;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(proxy);
        MinecraftForge.EVENT_BUS.register(TubeTicker.class);

        networkHandler.registerPacket(PacketStackJoinNetwork.class, Side.CLIENT);
        networkHandler.registerPacket(PacketStackLeaveNetwork.class, Side.CLIENT);
        networkHandler.registerPacket(PacketStackPickRoute.class, Side.CLIENT);
        networkHandler.registerPacket(PacketStackUpdate.class, Side.CLIENT);
        networkHandler.registerPacket(PacketModuleInfo.class, Side.SERVER);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();

        // Register GUI handler
        Technicalities.guiHandler.add(TKPneumaticsBlocks.pneumatic_tube, new TKPGuiHandler());

        try {
            Field field = TubeModule.ContainerModule.class.getDeclaredField("openGui");
            ReflectionHelper.makeFinalFieldModifiable(field);
            field.set(null, (BiConsumer<TubeModule.ContainerModule, EntityPlayer>) (mod, player) -> {
                BlockPos pos = mod.getTube().getTubePos();
                player.openGui(Technicalities.MODID, mod.getSide().ordinal(), mod.getTube().getTubeWorld(), pos.getX(), pos.getY(),
                        pos.getZ());
            });

            field = TubeModule.class.getDeclaredField("sendToServer");
            ReflectionHelper.makeFinalFieldModifiable(field);
            field.set(null, (BiConsumer<TubeModule, Consumer<PacketBuffer>>) (mod, cons) -> {
                PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
                cons.accept(buf);
                networkHandler.sendToServer(new PacketModuleInfo(mod.getTube().getTubePos(), mod.getSide(), buf));
            });
        } catch (Exception ex) {
            throw Throwables.propagate(ex);
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }

}

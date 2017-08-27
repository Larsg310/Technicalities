package com.technicalitiesmc.pneumatics;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.google.common.base.Throwables;
import com.technicalitiesmc.ITKModule;
import com.technicalitiesmc.ModuleProxy;
import com.technicalitiesmc.TKModule;
import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.api.pneumatics.TubeModule;
import com.technicalitiesmc.pneumatics.init.TKPneumaticsBlocks;
import com.technicalitiesmc.pneumatics.init.TKPneumaticsItems;
import com.technicalitiesmc.pneumatics.init.TKTubeModules;
import com.technicalitiesmc.pneumatics.network.PacketModuleInfo;
import com.technicalitiesmc.pneumatics.network.PacketStackJoinNetwork;
import com.technicalitiesmc.pneumatics.network.PacketStackLeaveNetwork;
import com.technicalitiesmc.pneumatics.network.PacketStackPickRoute;
import com.technicalitiesmc.pneumatics.network.PacketStackUpdate;
import com.technicalitiesmc.pneumatics.network.TKPGuiHandler;
import com.technicalitiesmc.pneumatics.tube.TubeTicker;
import com.technicalitiesmc.util.ReflectionUtils;
import com.technicalitiesmc.util.network.NetworkHandler;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;

@TKModule("pneumatics")
public class TKPneumatics implements ITKModule {

    @ModuleProxy(module = "pneumatics", serverSide = "com.technicalitiesmc.pneumatics.TKPCommonProxy",
            clientSide = "com.technicalitiesmc.pneumatics.client.TKPClientProxy")
    public static TKPCommonProxy proxy;

    public static final NetworkHandler NETWORK_HANDLER = new NetworkHandler(Technicalities.MODID + ".pneumatics");

    @Override
    public void preInit() {
        // Initialize and register blocks
        TKPneumaticsBlocks.initialize();
        TKPneumaticsBlocks.register();

        // Initialize and register items
        TKPneumaticsItems.initialize();
        TKPneumaticsItems.register();

        // Initialize and register tube modules
        TKTubeModules.initialize();
        TKTubeModules.register();

        MinecraftForge.EVENT_BUS.register(proxy);
        MinecraftForge.EVENT_BUS.register(TubeTicker.class);

        NETWORK_HANDLER.registerPacket(PacketStackJoinNetwork.class, Side.CLIENT);
        NETWORK_HANDLER.registerPacket(PacketStackLeaveNetwork.class, Side.CLIENT);
        NETWORK_HANDLER.registerPacket(PacketStackPickRoute.class, Side.CLIENT);
        NETWORK_HANDLER.registerPacket(PacketStackUpdate.class, Side.CLIENT);
        NETWORK_HANDLER.registerPacket(PacketModuleInfo.class, Side.SERVER);
    }

    @Override
    public void init() {
        proxy.init();

        // Register GUI handler
        Technicalities.guiHandler.add(TKPneumaticsBlocks.pneumatic_tube, new TKPGuiHandler());

        try {
            Field field = TubeModule.ContainerModule.class.getDeclaredField("openGui");
            ReflectionUtils.setModifier(field, Modifier.FINAL, false);
            field.setAccessible(true);
            field.set(null, (BiConsumer<TubeModule.ContainerModule, EntityPlayer>) (mod, player) -> {
                BlockPos pos = mod.getTube().getTubePos();
                player.openGui(Technicalities.MODID, mod.getSide().ordinal(), mod.getTube().getTubeWorld(), pos.getX(), pos.getY(),
                        pos.getZ());
            });

            field = TubeModule.class.getDeclaredField("sendToServer");
            ReflectionUtils.setModifier(field, Modifier.FINAL, false);
            field.setAccessible(true);
            field.set(null, (BiConsumer<TubeModule, Consumer<PacketBuffer>>) (mod, cons) -> {
                PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
                cons.accept(buf);
                NETWORK_HANDLER.sendToServer(new PacketModuleInfo(mod.getTube().getTubePos(), mod.getSide(), buf));
            });
        } catch (Exception ex) {
            throw Throwables.propagate(ex);
        }
    }

    @Override
    public void postInit() {

    }

}

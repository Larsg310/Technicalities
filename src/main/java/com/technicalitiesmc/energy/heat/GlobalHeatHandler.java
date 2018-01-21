package com.technicalitiesmc.energy.heat;

import com.google.common.base.Preconditions;
import com.technicalitiesmc.api.TechnicalitiesAPI;
import com.technicalitiesmc.base.Technicalities;
import com.technicalitiesmc.lib.util.DefaultCapabilityProvider;
import elec332.core.main.APIHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Elec332 on 28-12-2017.
 */
@APIHandler.StaticLoad
public enum GlobalHeatHandler {

    INSTANCE;

    GlobalHeatHandler(){
        DefaultCapabilityProvider.registerWorldCapabilityProvider(new ResourceLocation(Technicalities.MODID, "heatapi"), TechnicalitiesAPI.WORLD_HEAT_CAP, world -> new WorldHeatHandler());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void tickWorld(TickEvent.WorldTickEvent event){
        if (event.side == Side.CLIENT || event.phase == TickEvent.Phase.END){
            return;
        }
        WorldHeatHandler h = (WorldHeatHandler) event.world.getCapability(TechnicalitiesAPI.WORLD_HEAT_CAP, null);
        Preconditions.checkNotNull(h); //You never know...
        h.update(event.world, ((ChunkProviderServer) event.world.getChunkProvider()).id2ChunkMap.values());
    }
/*
    @SuppressWarnings("all")
    @SubscribeEvent(receiveCanceled = true)
    public void onNeighborChange(BlockEvent.NeighborNotifyEvent event){
        if (!event.getWorld().isRemote) {
            WorldHeatHandler h = (WorldHeatHandler) event.getWorld().getCapability(TechnicalitiesAPI.WORLD_HEAT_CAP, null);
            event.getNotifiedSides().forEach(facing -> {
                PositionedHeatData phd = h.getWorldData().get(event.getPos().offset(facing));
                if (phd != null){
                    phd.hasChanged();
                }
            });
        }
    }*/

    @SuppressWarnings("all")
    private class HeatCapHandler implements ICapabilitySerializable<NBTTagCompound> {

        private HeatCapHandler(){
            handler = new WorldHeatHandler();
        }

        private final WorldHeatHandler handler;

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == TechnicalitiesAPI.WORLD_HEAT_CAP;
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            return capability == TechnicalitiesAPI.WORLD_HEAT_CAP ? (T) handler : null;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return handler.serializeNBT();
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            handler.deserializeNBT(nbt);
        }

    }

}

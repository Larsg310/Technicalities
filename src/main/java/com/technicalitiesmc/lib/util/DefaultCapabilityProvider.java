package com.technicalitiesmc.lib.util;

import com.google.common.base.Preconditions;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by Elec332 on 16-1-2018.
 */
public class DefaultCapabilityProvider<C> implements ICapabilityProvider {

    public static <C> void registerWorldCapabilityProvider(ResourceLocation name, Capability<C> capability, Function<World, ? extends C> func){
        MinecraftForge.EVENT_BUS.register(new Object(){

            @SubscribeEvent
            public void attachCaps(AttachCapabilitiesEvent<World> event){
                event.addCapability(name, of(capability, (Supplier<C>) () -> func.apply(event.getObject())));
            }

        });
    }

    public static <C> ICapabilityProvider of(Capability<C> capability, Supplier<? extends C> impl){
        return of(capability, impl.get());
    }

    @SuppressWarnings("unchecked")
    public static <C, V extends C> ICapabilityProvider of(Capability<C> capability, V impl){
        return Preconditions.checkNotNull(impl) instanceof INBTSerializable ? new DefaultCapabilitySerializable<INBTSerializable>((Capability<INBTSerializable>) capability, (INBTSerializable) impl) : new DefaultCapabilityProvider<>(capability, impl);
    }

    private DefaultCapabilityProvider(Capability<C> capability, C impl){
        this.cap = capability;
        this.impl = impl;
    }

    private final Capability<C> cap;
    final C impl;

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == this.cap;
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == cap ? (T) impl : null;
    }

    private static class DefaultCapabilitySerializable<Q extends INBTSerializable> extends DefaultCapabilityProvider<Q> implements ICapabilitySerializable<NBTBase> {

        private DefaultCapabilitySerializable(Capability<Q> capability, Q impl) {
            super(capability, impl);
        }

        @Override
        public NBTBase serializeNBT() {
            return this.impl.serializeNBT();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void deserializeNBT(NBTBase nbt) {
            this.impl.deserializeNBT(nbt);
        }

    }

}

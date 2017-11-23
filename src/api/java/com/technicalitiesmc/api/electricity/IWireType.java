package com.technicalitiesmc.api.electricity;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

/**
 * Created by Elec332 on 6-11-2017.
 */
public interface IWireType extends IForgeRegistryEntry<IWireType> {

	public abstract double getResistivity();

	public abstract double getMassM3();

	public abstract Color getColor();

	@Nonnull
	@Override
	public ResourceLocation getRegistryName();

	@Override
	default public IWireType setRegistryName(ResourceLocation name){
		throw new UnsupportedOperationException();
	}

	@Override
	default public Class<IWireType> getRegistryType(){
		return IWireType.class;
	}
}

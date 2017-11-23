package com.technicalitiesmc.electricity.util;

import com.technicalitiesmc.api.electricity.IWireType;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.awt.*;

/**
 * Created by Elec332 on 23-11-2017.
 */
public enum EnumWireType implements IWireType {

	TEST(1, Color.BLACK, 1e10);

	EnumWireType(double resistivity, Color color, double massM3){
		this.resistivity = resistivity;
		this.color = color;
		this.mass = massM3;
	}

	private final Color color;
	private final double resistivity, mass;

	@Override
	public double getResistivity() {
		return this.resistivity;
	}

	@Override
	public double getMassM3() {
		return this.mass;
	}

	@Override
	public Color getColor() {
		return this.color;
	}

	@Nonnull
	@Override
	public ResourceLocation getRegistryName() {
		return new TKEResourceLocation(getName());
	}

	public String getName(){
		return toString().toLowerCase();
	}

}

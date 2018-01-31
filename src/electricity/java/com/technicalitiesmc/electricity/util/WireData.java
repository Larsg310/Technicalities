package com.technicalitiesmc.electricity.util;

import com.google.common.base.Preconditions;
import com.technicalitiesmc.api.electricity.EnumElectricityType;
import com.technicalitiesmc.api.electricity.IWireType;
import com.technicalitiesmc.api.electricity.WireConnectionMethod;
import com.technicalitiesmc.api.electricity.WireThickness;
import net.minecraft.item.EnumDyeColor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

/**
 * Created by Elec332 on 19-11-2017.
 */
public class WireData {

	public WireData(IWireType wireType, WireThickness thickness, WireConnectionMethod connectionMethod, EnumElectricityType energyType){
		this(wireType, thickness, connectionMethod, energyType, null);

	}

	public WireData(IWireType wireType, WireThickness thickness, WireConnectionMethod connectionMethod, EnumElectricityType energyType, EnumDyeColor color){
		this.wireType = Preconditions.checkNotNull(wireType);
		this.thickness = Preconditions.checkNotNull(thickness);
		this.connectionMethod = Preconditions.checkNotNull(connectionMethod);
		this.energyType = Preconditions.checkNotNull(energyType);
		this.color = color;
	}

	private IWireType wireType;
	private WireConnectionMethod connectionMethod;
	private WireThickness thickness;
	private EnumElectricityType energyType;
	@Nullable
	private EnumDyeColor color;

	public double getResistivity(double length){
		Preconditions.checkArgument(length > 0);
		return length * (wireType.getResistivity() / (thickness.surfaceAreaR * 0.001 * 0.001));
	}

	@Nonnull
	public EnumElectricityType getEnergyType() {
		return energyType;
	}

	@Nonnull
	public WireConnectionMethod getConnectionMethod() {
		return connectionMethod;
	}

	@Nullable
	public EnumDyeColor getColor() {
		return color;
	}

	public Color getWireTypeColor(){
		return wireType.getColor();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof WireData && equals_((WireData) obj);
	}

	private boolean equals_(WireData wireData){
		return wireType == wireData.wireType && connectionMethod == wireData.connectionMethod && thickness == wireData.thickness && energyType == wireData.energyType && color == wireData.color;
	}

}

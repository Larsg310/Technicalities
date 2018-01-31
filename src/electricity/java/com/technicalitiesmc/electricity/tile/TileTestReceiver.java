package com.technicalitiesmc.electricity.tile;

import com.technicalitiesmc.api.TechnicalitiesAPI;
import com.technicalitiesmc.api.electricity.EnumElectricityType;
import com.technicalitiesmc.api.electricity.IEnergyReceiver;
import com.technicalitiesmc.api.util.ConnectionPoint;
import elec332.core.api.info.IInfoDataAccessorBlock;
import elec332.core.api.info.IInfoProvider;
import elec332.core.api.info.IInformation;
import elec332.core.api.registration.RegisteredTileEntity;
import elec332.core.tile.TileBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Elec332 on 23-11-2017.
 */
@RegisteredTileEntity("testenergyreeiver")
public class TileTestReceiver extends TileBase implements IEnergyReceiver, IInfoProvider {

	private ConnectionPoint cp1, cp2;
	private double voltage, amps;

	@Override
	public void addInformation(@Nonnull IInformation iInformation, @Nonnull IInfoDataAccessorBlock iInfoDataAccessorBlock) {
		iInformation.add("Voltage: "+iInfoDataAccessorBlock.getData().getDouble("volts"));
		iInformation.add("Amps: "+iInfoDataAccessorBlock.getData().getDouble("amps"));
	}

	@Nonnull
	@Override
	public NBTTagCompound getInfoNBTData(@Nonnull NBTTagCompound nbtTagCompound, TileEntity tileEntity, @Nonnull EntityPlayerMP entityPlayerMP, @Nonnull IInfoDataAccessorBlock iInfoDataAccessorBlock) {
		nbtTagCompound.setDouble("volts", voltage);
		nbtTagCompound.setDouble("amps", amps);
		return nbtTagCompound;
	}

	@Nonnull
	@Override
	public EnumElectricityType getEnergyType(int post) {
		return EnumElectricityType.AC;
	}

	@Nonnull
	@Override
	public ConnectionPoint getConnectionPoint(int post) {
		return post == 0 ? cp1 : cp2;
	}

	@Nullable
	@Override
	public ConnectionPoint getConnectionPoint(EnumFacing side, Vec3d hitVec) {
		return side != getTileFacing().getOpposite() ? null : (hitVec.y > 0.5 ? cp2 : cp1);
	}

	@Override
	public void updateContainingBlockInfo() {
		super.updateContainingBlockInfo();
		createConnectionPoints();
	}

	@Override
	public void onLoad() {
		createConnectionPoints();
	}

	protected void createConnectionPoints() {
		cp1 = new ConnectionPoint(pos, world, getTileFacing().getOpposite(), 1);
		cp2 = new ConnectionPoint(pos, world, getTileFacing().getOpposite(), 2);
	}


	@Override
	public double getResistance() {
		return 3;
	}

	@Override
	public void receivePower(double voltage, double amps) {
		this.voltage = voltage;
		this.amps = amps;
	}

	@Override
	@SuppressWarnings("all")
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == TechnicalitiesAPI.ELECTRICITY_CAP || super.hasCapability(capability, facing);
	}

	@Override
	@SuppressWarnings("all")
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == TechnicalitiesAPI.ELECTRICITY_CAP ? (T) this : super.getCapability(capability, facing);
	}

}

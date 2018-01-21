package com.technicalitiesmc.energy.electricity.grid;

import com.google.common.collect.Lists;
import com.technicalitiesmc.api.TechnicalitiesAPI;
import com.technicalitiesmc.api.electricity.IElectricityDevice;
import com.technicalitiesmc.api.electricity.IEnergyObject;
import com.technicalitiesmc.api.util.ConnectionPoint;
import elec332.core.grid.IPositionable;
import elec332.core.world.DimensionCoordinate;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Elec332 on 13-11-2017.
 */
public final class ETileEntityLink implements IPositionable {

	protected ETileEntityLink(TileEntity tile) {
		pos = DimensionCoordinate.fromTileEntity(tile);
	}

	private final DimensionCoordinate pos;
	private ConnectionPoint[][] cp;

	@Nonnull
	@Override
	public DimensionCoordinate getPosition() {
		return pos;
	}

	@Override
	public boolean hasChanged() {
		if (cp == null){
			if (pos.isLoaded()){
				TileEntity tile = pos.getTileEntity();
				if (tile == null){
					return true;
				}
				IElectricityDevice device = tile.getCapability(TechnicalitiesAPI.ELECTRICITY_CAP, null);
				if (device == null){
					return true;
				}
				List<IEnergyObject> eio = Lists.newArrayList(device.getInternalComponents());

				cp = new ConnectionPoint[eio.size()][];
				for (int i = 0; i < cp.length; i++) {
					IEnergyObject eobj = eio.get(i);
					cp[i] = new ConnectionPoint[eobj.getPosts()];
					for (int j = 0; j < cp[i].length; j++) {
						cp[i][j] = eobj.getConnectionPoint(j);
					}
				}
				return true;
			} else {
				return false;
			}
		} else {
			if (pos.isLoaded()){
				TileEntity tile = pos.getTileEntity();
				if (tile == null){
					return true;
				}
				IElectricityDevice device = tile.getCapability(TechnicalitiesAPI.ELECTRICITY_CAP, null);
				if (device == null){
					return true;
				}
				List<IEnergyObject> eio = Lists.newArrayList(device.getInternalComponents());

				ConnectionPoint[][] newCP = new ConnectionPoint[eio.size()][];
				for (int i = 0; i < newCP.length; i++) {
					IEnergyObject eobj = eio.get(i);
					newCP[i] = new ConnectionPoint[eobj.getPosts()];
					for (int j = 0; j < newCP[i].length; j++) {
						newCP[i][j] = eobj.getConnectionPoint(j);
					}
				}

				if (newCP.length != cp.length){
					cp = newCP;
					return true;
				} else {
					boolean ch = false;
					for (int i = 0; i < cp.length; i++) {
						if (!Arrays.equals(newCP[i], cp[i])){
							ch = true;
							break;
						}
					}
					cp = newCP;
					return ch;
				}
			} else {
				cp = null;
				return true;
			}
		}
	}

}

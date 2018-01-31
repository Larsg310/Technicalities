package com.technicalitiesmc.api.electricity;

import com.technicalitiesmc.api.util.ConnectionPoint;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Elec332 on 12-11-2017.
 *
 * * NOTE: All functions defined in this interface, and all functions in interfaces that are
 * superclassed by this interface can be called asynchronously!
 */
public interface IEnergyObject {

	default public boolean canConnectTo(ConnectionPoint reqPoint, WireConnectionMethod type, IWireType wireData){
		return true;
	}

	@Nonnull
	public EnumElectricityType getEnergyType(int post);

	@Nonnull
	public ConnectionPoint getConnectionPoint(int post);

	@Nullable
	public ConnectionPoint getConnectionPoint(EnumFacing side, Vec3d hitVec);

	default public int getPosts(){
		return 2;
	}

	default public String getDescription(int post){
		return toString() + " post "+post;
	}

	@Nullable
	default public Vec3d getConnection(ConnectionPoint cp){
		return null;
	}

	default public boolean isPassiveConnector(){
		return false;
	}

}

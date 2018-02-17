package com.technicalitiesmc.electricity.compat.tkmechanical;

import com.technicalitiesmc.api.electricity.EnumElectricityType;
import com.technicalitiesmc.api.electricity.IEnergyReceiver;
import com.technicalitiesmc.api.mechanical.IKineticNode;
import com.technicalitiesmc.api.util.ConnectionPoint;
import com.technicalitiesmc.api.util.ObjFloatConsumer;
import elec332.core.api.registration.RegisteredTileEntity;
import elec332.core.tile.TileBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiPredicate;

/**
 * Created by Elec332 on 23-11-2017.
 */
@RegisteredTileEntity("TKElectricityElectricalEngine")
public class TileElectricalEngine extends TileBase implements IEnergyReceiver, IKineticNode.Host {

	private ConnectionPoint cp1, cp2;
	private double currentVoltage, currentAmps;
	private static final double efficiency = 0.97;
	// :( impossible
	// private IKineticNode outputNode = new ServerKineticNode()

	@Override
	public void updateContainingBlockInfo() {
		super.updateContainingBlockInfo();
		createConnectionPoints();
	}

	@Override
	public void onLoad() {
		createConnectionPoints();
	}

	protected void createConnectionPoints(){
		cp1 = new ConnectionPoint(pos, world, getTileFacing().getOpposite(), 1);
		cp2 = new ConnectionPoint(pos, world, getTileFacing().getOpposite(), 2);
	}

	@Override
	public double getResistance() {
		return 5;
	}

	@Override
	public void receivePower(double voltage, double amps) {
		this.currentVoltage = voltage;
		this.currentAmps = amps;
	}

	@Nonnull
	@Override
	public EnergyType getEnergyType(int post) {
		return EnergyType.AC;
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
	public World getKineticWorld() {
		return null;
	}

	@Override
	public ChunkPos getKineticChunk() {
		return null;
	}

	@Override
	public float getInertia() {
		return 1;
	}

	@Override
	public float getAppliedPower() {
		return (float) (currentVoltage * currentAmps * efficiency);
	}

	@Override
	public void addNeighbors(ObjFloatConsumer<IKineticNode> neighbors, BiPredicate<World, BlockPos> posValidator) {

	}

}

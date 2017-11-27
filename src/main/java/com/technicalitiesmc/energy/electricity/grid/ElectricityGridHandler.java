package com.technicalitiesmc.energy.electricity.grid;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.technicalitiesmc.api.TechnicalitiesAPI;
import com.technicalitiesmc.api.electricity.IEnergyObject;
import com.technicalitiesmc.api.electricity.component.CircuitElement;
import com.technicalitiesmc.api.electricity.component.ICircuit;
import com.technicalitiesmc.api.util.ConnectionPoint;
import com.technicalitiesmc.base.Technicalities;
import com.technicalitiesmc.base.TechnicalitiesConfig;
import com.technicalitiesmc.energy.electricity.simulation.CircuitElementFactory;
import com.technicalitiesmc.energy.electricity.simulation.engine.Circuit;
import com.technicalitiesmc.energy.electricity.simulation.engine.SimulationEngine;
import elec332.core.grid.AbstractGridHandler;
import elec332.core.world.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

/**
 * Created by Elec332 on 13-11-2017.
 */
public final class ElectricityGridHandler extends AbstractGridHandler<ETileEntityLink> {

	public ElectricityGridHandler(){
		MinecraftForge.EVENT_BUS.register(this);
		this.cache = new DefaultMultiWorldPositionedObjectHolder<>();
		this.circuits = Maps.newHashMap();
		this.toAddNextTick = Sets.newHashSet();
	}

	//Circuit circuit = new Circuit();
	private Multimap<Object, CircuitElement<?>> map = HashMultimap.create();
	private IMultiWorldPositionedObjectHolder<CPPosObj> cache;
	public Set<IEnergyObject> wires = Sets.newHashSet();
	public final Set<IEnergyObject> wirez = Collections.unmodifiableSet(wires);
	private Map<UUID, Circuit> circuits;
	private Set<CircuitElement<?>> toAddNextTick;

	//temp
	public void addWire(IEnergyObject wire){
		if (wires.add(wire)){
			Collection<CircuitElement<?>> elm = CircuitElementFactory.INSTANCE.wrapComponent(wire);
			elm.forEach(element -> {
				map.put(wire, element);
				addElmImpl(element);
			});
		}
	}

	//temp
	public void removeWire(IEnergyObject wire){
		Collection<CircuitElement<?>> elm = map.get(wire);
		if (elm != null){
			elm.forEach(this::removeElmImpl);
			map.removeAll(wire);
			wires.remove(wire);
		}
	}

	public void clear(){
		map.clear();
		cache.clear();
		circuits.values().forEach(Circuit::clear);
		circuits.clear();
		wires.clear();
	}

	@Override
	protected void onObjectRemoved(ETileEntityLink o, Set<DimensionCoordinate> allUpdates) {
		Collection<CircuitElement<?>> elm = map.get(o);
		if (elm != null){
			elm.forEach(this::removeElmImpl);
			map.removeAll(o);
		}
	}

	@Override
	protected void internalAdd(ETileEntityLink o) {
		if (!toAddNextTick.isEmpty()){
			toAddNextTick.forEach(this::addElmImpl);
			toAddNextTick.clear();
		}
		TileEntity tile = o.getPosition().getTileEntity();
		if (tile == null){
			return;
		}
		Collection<CircuitElement<?>> elm = CircuitElementFactory.INSTANCE.wrapComponent(tile.getCapability(TechnicalitiesAPI.ELECTRICITY_CAP, null));
		elm.forEach(element -> {
			map.put(o, element);
			addElmImpl(element);
		});
	}

	private void addElmImpl(CircuitElement<?> elm){
		if (elm == null){
			return;
		}
		Set<CircuitElement> ceL = Sets.newHashSet(elm);
		Circuit myCircuit = null;
		for (ConnectionPoint cp : elm.getConnectionPoints()){
			PositionedObjectHolder<CPPosObj> woj = cache.getOrCreate(cp.getWorld());
			CPPosObj bla = woj.get(cp.getPos());
			if (bla == null){
				woj.put(bla = new CPPosObj(), cp.getPos());
			}
			for (CircuitElement<?> otherElmsAtPos : bla.connections.get(cp)){
				ICircuit c = otherElmsAtPos.getCircuit();
				if (c == null){
					if (myCircuit == null) {
						ceL.add(otherElmsAtPos);
					} else {
						myCircuit.addElement(otherElmsAtPos);
					}
				} else {
					Circuit circuit = (Circuit) c;
					if (myCircuit == null) {
						ceL.forEach(circuit::addElement);
						ceL = null;
						myCircuit = circuit;
					} else {
						if (circuit != myCircuit) {
							myCircuit.consumeCircuit(circuit);
							circuit.clear();
							circuits.remove(circuit.getId());
						}
					}
				}
			}
			bla.connections.put(cp, elm);
		}
		if (ceL != null && ceL.size() > 1){
			Circuit newCircuit = new Circuit();
			ceL.forEach(newCircuit::addElement);
			circuits.put(newCircuit.getId(), newCircuit);
		}
	}

	private void removeElmImpl(CircuitElement<?> elm){
		if (elm == null){
			return;
		}
		ICircuit c = elm.getCircuit();
		if (c == null){
			toAddNextTick.remove(elm);
			return;
		}
		Circuit circuit = (Circuit) c;
		if (circuit.removeElement(elm, toAddNextTick)){
			circuits.remove(circuit.getId());
			circuit.clear();
		}
		for (ConnectionPoint cp : elm.getConnectionPoints()){
			PositionedObjectHolder<CPPosObj> woj = cache.getOrCreate(cp.getWorld());
			CPPosObj bla = woj.get(cp.getPos());
			if (bla == null){
				throw new RuntimeException();
			}
			bla.connections.remove(cp, elm);
		}
		elm.destroy();
	}

	@Override
	public void tick() {
		if (TechnicalitiesConfig.Debug.extremeElectricityGridDebug) {
			System.out.println("-------------------------------");
			System.out.println("Siz: " + circuits.keySet().size());
		}
		//SimEngine.INSTANCE.preTick(circuit);
		circuits.values().forEach(SimulationEngine.INSTANCE::preTick);
	}

	@Override
	public boolean isValidObject(TileEntity tile) {
		if (!tile.hasCapability(TechnicalitiesAPI.ELECTRICITY_CAP, null)){
			return false;
		}
		IEnergyObject eObj = tile.getCapability(TechnicalitiesAPI.ELECTRICITY_CAP, null);
		return eObj != null && !eObj.isPassiveConnector();
	}

	@Override
	protected ETileEntityLink createNewObject(TileEntity tile) {
		return new ETileEntityLink(tile);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void serverTick(TickEvent.ServerTickEvent event){
		if (event.side.isServer() && event.phase == TickEvent.Phase.END) {
			if (TechnicalitiesConfig.Debug.extremeElectricityGridDebug) {
				System.out.println("TickLow: " + FMLCommonHandler.instance().getMinecraftServerInstance().worlds[0].getWorldTime());
			}

			circuits.values().forEach(SimulationEngine.INSTANCE::tick);
			//SimEngine.INSTANCE.tick(circuit);
			if (TechnicalitiesConfig.Debug.extremeElectricityGridDebug) {
				System.out.println("-------------------------");
			}
		}
	}

	@SubscribeEvent
	public void worldUnload(WorldEvent.Unload event){
		if (!event.getWorld().isRemote && WorldHelper.getDimID(event.getWorld()) == 0){
			Technicalities.electricityGridHandler.clear();
		}
	}

}

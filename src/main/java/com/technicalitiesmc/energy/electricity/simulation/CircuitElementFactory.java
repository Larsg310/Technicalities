package com.technicalitiesmc.energy.electricity.simulation;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.technicalitiesmc.api.electricity.IElectricityDevice;
import com.technicalitiesmc.api.electricity.IEnergyObject;
import com.technicalitiesmc.api.electricity.IEnergyReceiver;
import com.technicalitiesmc.api.electricity.IEnergySource;
import com.technicalitiesmc.api.electricity.component.CircuitElement;
import com.technicalitiesmc.energy.electricity.simulation.optimization.ResistorOptimizer;
import elec332.core.main.APIHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Created by Elec332 on 12-11-2017.
 */
@APIHandler.StaticLoad
public enum CircuitElementFactory {

	INSTANCE;

	CircuitElementFactory(){
		this.cache = Maps.newHashMap();
		this.elementCheckers = Maps.newHashMap();
	}

	private Map<Class<?>, BiConsumer<IEnergyObject, Collection<CircuitElement<?>>>> cache;
	private List<ICircuitCompressor> optimizers = Lists.newArrayList();
	private Map<Integer, Pair<Class, IElementChecker>> elementCheckers;
	private int hc = 1000;

	@Nonnull
	public Collection<CircuitElement<?>> wrapComponent(IElectricityDevice component){
		if (component == null){
			return Collections.emptySet();
		}
		Set<IEnergyObject> objects = component instanceof IEnergyObject ? Sets.newHashSet((IEnergyObject) component) : component.getInternalComponents();
		Set<CircuitElement<?>> ret = Sets.newHashSet();
		objects.forEach(object -> ret.addAll(wrapComponent(object)));
		return ret;
	}

	@Nonnull
	public Set<CircuitElement<?>> wrapComponent(IEnergyObject component){
		List<BiConsumer<IEnergyObject, Collection<CircuitElement<?>>>> wrappers = Lists.newArrayList();
		cache.forEach((type, wrapper) -> {
			if (type.isAssignableFrom(component.getClass())) {
				wrappers.add(wrapper);
			}
		});
		int i = wrappers.size();
		if (i == 1){
			Set<CircuitElement<?>> ret = Sets.newHashSet();
			wrappers.get(0).accept(component, ret);
			Preconditions.checkArgument(!ret.isEmpty());
			return ret;
		} else if (i == 0){
			throw new IllegalArgumentException(component.toString());
		} else {
			throw new IllegalStateException(component.toString());
		}
	}

	public boolean isPassiveConnector(IElectricityDevice device){
		for (IEnergyObject obj : device.getInternalComponents()){
			if (!obj.isPassiveConnector()){
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("all")
	public <O extends IEnergyObject, E extends CircuitElement<?>> void registerComponentWrapper(Class<O> clazz, BiConsumer<O, Collection<CircuitElement<?>>> wrapper){
		cache.put(clazz, (BiConsumer<IEnergyObject, Collection<CircuitElement<?>>>) wrapper);
	}

	@SuppressWarnings("all")
	public <O extends IEnergyObject, E extends CircuitElement<O>> void registerComponentWrapper(Class<O> clazz, Class<E> eClass, Function<O, E> wrapper, int weight, IElementChecker<E> checker){
		cache.put(clazz, (energyObject, circuitElements) -> circuitElements.add(wrapper.apply((O)energyObject)));
		if (elementCheckers.containsKey(weight)){
			throw new IllegalArgumentException();
		}
		elementCheckers.put(weight, Pair.of(eClass, checker));
	}

	public <O extends IEnergyObject, E extends CircuitElement<O>> void registerComponentWrapper(Class<O> clazz, Class<E> eClass, Function<O, E> wrapper){
		registerComponentWrapper(clazz, eClass, wrapper, hc++, elements -> true);
	}

	public List<ICircuitCompressor> getCircuitOptimizers(){
		return optimizers;
	}

	public Collection<Pair<Class, IElementChecker>> getElementCheckers() {
		return elementCheckers.values();
	}

	static {
		INSTANCE.registerComponentWrapper(IEnergyReceiver.class, ResistorElement.class, ResistorElement::new);
		INSTANCE.registerComponentWrapper(IEnergySource.class, VoltageElement.class, VoltageElement::new);
		INSTANCE.optimizers.add(new ResistorOptimizer());
	}

}

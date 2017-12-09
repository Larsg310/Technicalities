package com.technicalitiesmc.base.tile;

import com.technicalitiesmc.lib.block.TileBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class TileValve extends TileBase {

    private final TankInterface itf1 = new TankInterface();
    private final TankInterface itf2 = new TankInterface();

    public TankInterface getInterface(EnumFacing.AxisDirection dir) {
        return dir == EnumFacing.AxisDirection.NEGATIVE ? itf1 : itf2;
    }

    public class TankInterface {

        private final Map<FluidStack, Integer> fluids = new TreeMap<>(Comparator.comparingInt((FluidStack s) -> s.getFluid().getDensity(s))
                .thenComparing(Comparator.comparing((FluidStack s) -> s.getFluid().getName())));

        private TankInterface() {
        }

        private Set<TileValve> valves = new HashSet<>();

        public List<FluidStack> getFluids() {
            return Collections.emptyList();
        }

    }

}

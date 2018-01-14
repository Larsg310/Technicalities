package com.technicalitiesmc.base.tank;

import com.technicalitiesmc.base.tile.TileValve;
import com.technicalitiesmc.lib.pool.DataPool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

public class MultiblockTank implements INBTSerializable<NBTTagCompound> {

    private static final DataPool<MultiblockTank> TANKS = new DataPool<>();

    public static Collection<MultiblockTank> getAllTanks() {
        return TANKS.getAll();
    }

    public static boolean formTank(BlockPos corner, Vec3i dimensions, Set<TileValve.Valve> valves, TileValve.Valve master, int capacity) {
        MultiblockTank tank = new MultiblockTank(corner, dimensions, valves, master, capacity);
        if (!tank.isValid()) {
            return false;
        }
        UUID uuid = TANKS.register(tank);
        valves.forEach(v -> v.setTank(TANKS.getHandle(uuid)));
        return true;
    }

    public static void unformTank(TileValve.Valve valve) {
        UUID uuid = valve.getTankID();
        MultiblockTank tank = TANKS.getObject(uuid);
        tank.unlink();
        TANKS.unregister(uuid);
    }

    private final Map<FluidStack, Integer> fluids = new TreeMap<>(Comparator.comparingInt((FluidStack s) -> s.getFluid().getDensity(s)).reversed()
            .thenComparing(Comparator.comparing((FluidStack s) -> s.getFluid().getName())));

    private final BlockPos corner;
    private final Vec3i dimensions;
    private final Set<TileValve.Valve> valves;
    private final TileValve.Valve master;
    private final int capacity;

    public MultiblockTank(BlockPos corner, Vec3i dimensions, Set<TileValve.Valve> valves, TileValve.Valve master, int capacity) {
        this.corner = corner;
        this.dimensions = dimensions;
        this.valves = valves;
        this.master = master;
        this.capacity = capacity;
    }

    public BlockPos getCorner() {
        return corner;
    }

    public Vec3i getDimensions() {
        return dimensions;
    }

    public int getStored() {
        return fluids.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean isValid() {
        return getStored() <= getCapacity();
    }

    private void unlink() {
        valves.forEach(v -> v.setTank(null));
        master.storeTankInfo(serializeNBT());
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {

    }

    public int fill(FluidStack fluid, boolean simulate) {
        int max = getCapacity() - getStored();
        int inserted = Math.min(fluid.amount, max);
        if (max > 0 && !simulate) {
            fluid = fluid.copy();
            fluid.amount = 1;
            int stored = fluids.getOrDefault(fluid, 0);
            fluids.put(fluid, stored + inserted);
        }
        return inserted;
    }

    public FluidStack drain(float height, FluidStack fluid, int amount, boolean simulate) {
        height /= getDimensions().getY();
        height *= getCapacity();
        int cumulativeHeight = 0;
        for (Map.Entry<FluidStack, Integer> entry : fluids.entrySet()) {
            if (cumulativeHeight + entry.getValue() >= height && (fluid == null || fluid.isFluidEqual(entry.getKey()))) {
                int accessibleAmt = (int) Math.min(cumulativeHeight - height, 0) + entry.getValue();
                int drained = Math.min(accessibleAmt, amount);
                FluidStack out = entry.getKey().copy();
                out.amount = drained;
                if (!simulate) {
                    int amt = entry.getValue() - drained;
                    if (amt > 0) {
                        fluids.put(entry.getKey(), amt);
                    } else {
                        fluids.remove(entry.getKey());
                    }
                }
                return out;
            }
            cumulativeHeight += entry.getValue();
        }
        return null;
    }

    public IFluidTankProperties[] getTankProperties(float height) {
        height /= (float) getDimensions().getY() / getCapacity();
        int cumulativeHeight = 0;
        List<IFluidTankProperties> properties = new ArrayList<>();
        for (Map.Entry<FluidStack, Integer> entry : fluids.entrySet()) {
            FluidStack stack = entry.getKey().copy();
            if (cumulativeHeight >= height) {
                int accessibleAmt = (int) Math.min(cumulativeHeight - height, 0) + entry.getValue();
                if (accessibleAmt == entry.getValue()) {
                    // Fully accessible
                    stack.amount = entry.getValue();
                    properties.add(new FluidTankProperties(stack, capacity, true, true));
                } else {
                    // Partly accessible
                    stack.amount = accessibleAmt;
                    properties.add(new FluidTankProperties(stack, capacity, true, true));

                    stack = stack.copy();
                    stack.amount = entry.getValue() - accessibleAmt;
                    properties.add(new FluidTankProperties(stack, capacity, false, false));
                }
            } else {
                // Not accessible
                stack.amount = entry.getValue();
                properties.add(new FluidTankProperties(stack, capacity, false, false));
            }

            cumulativeHeight += entry.getValue();
        }
        return properties.toArray(new IFluidTankProperties[properties.size()]);
    }

    public Map<FluidStack, Integer> getFluids() {
        return fluids;
    }
}

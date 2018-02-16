package com.technicalitiesmc.base.tile;

import com.technicalitiesmc.api.TechnicalitiesAPI;
import com.technicalitiesmc.api.heat.IHeatConductor;
import com.technicalitiesmc.api.heat.IThermalMaterial;
import com.technicalitiesmc.base.block.BlockHeatPipe;
import com.technicalitiesmc.base.init.TKBaseBlocks;
import com.technicalitiesmc.lib.block.TileFloatingPipeBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class TileHeatPipe extends TileFloatingPipeBase implements IHeatConductor {
    private Map<EnumFacing, Boolean> overrides = new EnumMap<>(EnumFacing.class);

    @Override
    public boolean testConnectionOnSide(EnumFacing side) {
        boolean autoConnect = super.testConnectionOnSide(side) ||
            Optional.ofNullable(getWorld().getTileEntity(pos))
                .filter(it -> it.hasCapability(TechnicalitiesAPI.HEAT_CONDUCTOR_CAP, null))
                .map(it -> it.getCapability(TechnicalitiesAPI.HEAT_CONDUCTOR_CAP, null))
                .map(it -> it.touches(side.getOpposite()))
                .orElse(false);

        if (overrides.containsKey(side) && overrides.get(side).equals(autoConnect)) {
            overrides.remove(side);
        }

        return autoConnect ^ overrides.containsKey(side);
    }

    public void toggleSide(EnumFacing side) {
        if (getWorld().isRemote) return;
        overrides.put(side, !isConnected(side));
        updateConnections();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        short amap = (short) (int) Arrays.stream(EnumFacing.VALUES)
            .map(it -> ((overrides.containsKey(it) ? 1 : 0) | (overrides.getOrDefault(it, false) ? 2 : 0)) << (it.getIndex() * 2))
            .reduce(0, (acc, it) -> acc | it);
        compound.setShort("overrides", amap);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        short amap = compound.getShort("overrides");
        overrides.clear();
        Arrays.stream(EnumFacing.VALUES)
            .map(it -> Pair.of(it, (amap >> (it.getIndex() * 2)) & 3))
            .filter(it -> (it.getRight() & 1) != 0)
            .map(it -> Pair.of(it.getLeft(), (it.getRight() & 2) != 0))
            .forEach(it -> overrides.put(it.getLeft(), it.getRight()));
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public IThermalMaterial getMaterial() {
        if (getBlockType() == TKBaseBlocks.heat_pipe_small) {
            return BlockHeatPipe.ThermalMaterial.SMALL;
        } else if (getBlockType() == TKBaseBlocks.heat_pipe_medium) {
            return BlockHeatPipe.ThermalMaterial.MEDIUM;
        } else if (getBlockType() == TKBaseBlocks.heat_pipe_large) {
            return BlockHeatPipe.ThermalMaterial.LARGE;
        } else {
            throw new IllegalStateException("Wait, what?");
        }
    }

    @Override
    public boolean touches(@Nonnull EnumFacing side) {
        return isConnected(side);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return getCapability(capability, facing) != null || super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == TechnicalitiesAPI.HEAT_CONDUCTOR_CAP && facing == null) {
            return (T) this;
        } else {
            return super.getCapability(capability, facing);
        }
    }
}

package com.technicalitiesmc.lib.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TileFloatingPipeBase extends TileBase {
    private Set<EnumFacing> connections = EnumSet.noneOf(EnumFacing.class);

    public final boolean isConnected(EnumFacing side) {
        return connections.contains(side);
    }

    public void updateConnections() {
        connections.clear();
        connections.addAll(
            Arrays.stream(EnumFacing.VALUES)
                .filter(this::testConnectionOnSide)
                .collect(Collectors.toSet()));
        sync(); // TODO: only update the connection data?
        sendPacket(-10, new NBTTagCompound()); // render update
    }

    @Override
    public void onDataPacket(int id, NBTTagCompound tag) {
        if (id == -10) syncData();
        else super.onDataPacket(id, tag);
    }

    public boolean testConnectionOnSide(EnumFacing side) {
        return getWorld().getBlockState(getPos().offset(side)).getBlock() == getBlockType();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setByte("_c_set", (byte) connections.stream()
            .mapToInt(Enum::ordinal)
            .map(it -> 1 << it)
            .reduce(0, (i1, i2) -> i1 | i2));
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        int cIn = compound.getByte("_c_set");
        connections.clear();
        connections.addAll(
            Arrays.stream(EnumFacing.VALUES)
                .filter(it -> (cIn & (1 << it.ordinal())) != 0)
                .collect(Collectors.toSet()));
    }
}

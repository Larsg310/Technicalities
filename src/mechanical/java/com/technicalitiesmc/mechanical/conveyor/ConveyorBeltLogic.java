package com.technicalitiesmc.mechanical.conveyor;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.technicalitiesmc.api.mechanical.conveyor.IConveyorBelt;
import com.technicalitiesmc.api.mechanical.conveyor.IConveyorObject;

import net.minecraft.util.EnumFacing;

public class ConveyorBeltLogic implements IConveyorBelt {

    private final IConveyorBeltHost host;
    private final float height;

    private final Set<Pair<IConveyorObject, Path>> objects = new HashSet<>();

    public ConveyorBeltLogic(IConveyorBeltHost host, float height) {
        this.host = host;
        this.height = height;
    }

    public void tick() {
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public EnumFacing getDirection() {
        return null;
    }

    @Override
    public boolean canInput(EnumFacing side) {
        return false;
    }

    @Override
    public void insert(EnumFacing side, IConveyorObject object) {

    }

    public Set<Pair<IConveyorObject, Path>> getObjects() {
        return objects;
    }

    public class Path {

    }

}

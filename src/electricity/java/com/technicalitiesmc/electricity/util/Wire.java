package com.technicalitiesmc.electricity.util;

import com.technicalitiesmc.api.electricity.EnumElectricityType;
import com.technicalitiesmc.api.electricity.IEnergyObject;
import com.technicalitiesmc.api.electricity.WireConnectionMethod;
import com.technicalitiesmc.api.util.ConnectionPoint;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Elec332 on 7-11-2017.
 */
public class Wire implements IEnergyObject {

    private Vec3d start, end, renderStart, renderEnd;
    private ConnectionPoint startPos, endPos;
    private WireData wireData;

    public Wire(ConnectionPoint start, Vec3d startV, ConnectionPoint end, Vec3d endV, WireData wireData) {
        boolean n = start.hashCode() > end.hashCode();
        this.startPos = n ? start : end;
        this.endPos = n ? end : start;
        n = startV.hashCode() > endV.hashCode();
        this.start = n ? startV : endV;
        this.end = n ? endV : startV;
        this.renderStart = this.start.addVector(startPos.getPos().getX(), startPos.getPos().getY(), startPos.getPos().getZ());
        this.renderEnd = this.end.addVector(endPos.getPos().getX(), endPos.getPos().getY(), endPos.getPos().getZ());
        this.wireData = wireData;
    }

    public Vec3d getStart() {
        return renderStart;
    }

    public Vec3d getEnd() {
        return renderEnd;
    }

    public double getLength() {
        return start.distanceTo(end);
    }

    public void drop() {

    }

    public boolean isOverhead() {
        return wireData.getConnectionMethod() == WireConnectionMethod.OVERHEAD;
    }

    public double getResistance() {
        return wireData.getResistivity(getLength());
    }

    @Nonnull
    @Override
    public EnumElectricityType getEnergyType(int post) {
        return wireData.getEnergyType();
    }

    @Nonnull
    @Override
    public ConnectionPoint getConnectionPoint(int post) {
        return post == 0 ? startPos : endPos;
    }

    @Nullable
    @Override
    public ConnectionPoint getConnectionPoint(EnumFacing side, Vec3d hitVec) {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Wire ? eq((Wire) obj) : super.equals(obj);
    }

    public boolean eq(Wire wire) {
        return start.equals(wire.start) && end.equals(wire.end) && startPos.equals(wire.startPos) && endPos.equals(wire.endPos) && wireData.equals(wire.wireData);
    }

}

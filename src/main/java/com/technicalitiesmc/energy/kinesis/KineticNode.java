package com.technicalitiesmc.energy.kinesis;

import com.technicalitiesmc.api.mechanical.IKineticNode;
import com.technicalitiesmc.api.util.ObjFloatConsumer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.UUID;
import java.util.function.BiPredicate;

public class KineticNode implements IKineticNode {

    private final IKineticNode.Host host;
    private UUID nodeID = UUID.randomUUID();
    private boolean visited = false, isRemote;
    private float torque, velocity, power, angle;
    private float ratio;

    public KineticNode(Host host) {
        this.host = host;
    }

    public World getWorld() {
        return host.getKineticWorld();
    }

    public ChunkPos getChunk() {
        return host.getKineticChunk();
    }

    public UUID getNodeID() {
        return nodeID;
    }

    @Override
    public void validate(boolean isRemote) {
        this.isRemote = isRemote;
        if (isRemote) {
            ClientKineticManager.INSTANCE.add(this);
        } else {
            ServerKineticManager.INSTANCE.add(this);
        }
    }

    @Override
    public void invalidate() {
        if (isRemote) {
            ClientKineticManager.INSTANCE.remove(this);
        } else {
            ServerKineticManager.INSTANCE.remove(this);
        }
    }

    @Override
    public float getTorque() {
        return torque;
    }

    @Override
    public float getVelocity() {
        return velocity;
    }

    @Override
    public float getPower() {
        return power;
    }

    @Override
    public float getAngle() {
        return angle;
    }

    @Override
    public float getInertia() {
        return host.getInertia();
    }

    @Override
    public float getAppliedPower() {
        return host.getAppliedPower();
    }

    @Override
    public float getConsumedPower() {
        return host.getConsumedPower();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setUniqueId("id", nodeID);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        nodeID = tag.getUniqueId("id");
    }

    @Override
    public String toString() {
        return "KineticNode(torque=" + torque + ", velocity=" + velocity + ", power=" + power + ", angle=" + angle + ")";
    }

    public void addNeighbors(ObjFloatConsumer<IKineticNode> neighbors, BiPredicate<World, BlockPos> posValidator) {
        host.addNeighbors(neighbors, posValidator);
    }

    void update(float torque, float velocity, float power) {
        this.torque = torque;
        this.velocity = velocity;
        this.power = power;
        this.angle = this.angle + velocity;
    }

    public boolean isVisited() {
        return visited;
    }

    public void markVisited() {
        visited = true;
    }

    public void resetVisitState() {
        visited = false;
    }

    public float getRatio() {
        return ratio;
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
    }

}

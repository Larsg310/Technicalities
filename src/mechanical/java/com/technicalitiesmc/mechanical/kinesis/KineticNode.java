package com.technicalitiesmc.mechanical.kinesis;

import com.technicalitiesmc.api.mechanical.IKineticNode;
import com.technicalitiesmc.api.util.ObjFloatConsumer;

public class KineticNode implements IKineticNode {

    private final IKineticNode.Host host;

    private float torque, velocity, power;
    private boolean visited = false;

    public KineticNode(Host host) {
        this.host = host;
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

    public void addNeighbors(ObjFloatConsumer<IKineticNode> neighbors) {
        host.addNeighbors(neighbors);
    }

    public void update(float torque, float velocity, float power) {
        this.torque = torque;
        this.velocity = velocity;
        this.power = power;
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

    @Override
    public String toString() {
        return "KineticNode(torque=" + torque + ", velocity=" + velocity + ", power=" + power + ")";
    }

}

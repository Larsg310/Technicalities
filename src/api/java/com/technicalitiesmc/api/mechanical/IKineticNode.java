package com.technicalitiesmc.api.mechanical;

import com.technicalitiesmc.api.util.ObjFloatConsumer;

public interface IKineticNode {

    public float getTorque();

    public float getVelocity();

    public float getPower();

    // public float getAngle(float partialTicks);

    public float getInertia(); // Inherited from host

    public float getAppliedPower(); // Inherited from host

    public float getConsumedPower(); // Inherited from host

    public interface Host {

        public float getInertia();

        public default float getAppliedPower() {
            return 0;
        }

        public default float getConsumedPower() {
            return getInertia();
        }

        public void addNeighbors(ObjFloatConsumer<IKineticNode> neighbors);

    }

}

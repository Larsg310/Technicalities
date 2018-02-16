package com.technicalitiesmc.api.mechanical;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public interface IShaftAttachable {

    @CapabilityInject(IShaftAttachable.class)
    public static final Capability<IShaftAttachable> CAPABILITY = null;

    public IKineticNode getNode();

}

package com.technicalitiesmc.api.mechanical;

import com.technicalitiesmc.lib.capability.SimpleCapability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

@SimpleCapability
public interface IShaftAttachable {

    @CapabilityInject(IShaftAttachable.class)
    public static final Capability<IShaftAttachable> CAPABILITY = null;

    public IKineticNode getNode(boolean internal);

}

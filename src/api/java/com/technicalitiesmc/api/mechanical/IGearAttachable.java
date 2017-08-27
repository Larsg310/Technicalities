package com.technicalitiesmc.api.mechanical;

import com.technicalitiesmc.lib.capability.SimpleCapability;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

@SimpleCapability
public interface IGearAttachable {

    @CapabilityInject(IGearAttachable.class)
    public static final Capability<IGearAttachable> CAPABILITY = null;

    public IKineticNode getNode(EnumFacing faceSide);

}

package com.technicalitiesmc.mechanical.conveyor.object;

import com.technicalitiesmc.api.mechanical.conveyor.IConveyorObject;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import java.util.UUID;

public abstract class ConveyorObject implements IConveyorObject {
    private UUID uuid = UUID.randomUUID();

    @Nonnull
    @Override
    public UUID uuid() {
        return uuid;
    }

    @Override
    public void saveData(NBTTagCompound tag) {
        tag.setUniqueId("ID", uuid);
    }

    @Override
    public void loadData(NBTTagCompound tag) {
        uuid = tag.getUniqueId("ID");
    }
}

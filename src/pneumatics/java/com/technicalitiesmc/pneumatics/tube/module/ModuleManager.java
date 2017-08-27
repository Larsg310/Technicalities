package com.technicalitiesmc.pneumatics.tube.module;

import java.util.List;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.api.pneumatics.TubeModule;
import com.technicalitiesmc.util.simple.SimpleRegistry;

import net.minecraftforge.registries.ForgeRegistry;

public enum ModuleManager {
    INSTANCE;

    @SimpleRegistry(value = Technicalities.MODID + ":pneumatics.tube_module", type = TubeModule.Type.class)
    public static ForgeRegistry<TubeModule.Type<?>> registry;

    public List<TubeModule.Type<?>> getModuleTypes() {
        return registry.getValues();
    }

    public TubeModule.Type<?> get(int id) {
        return registry.getValue(id);
    }

    public int getID(TubeModule.Type<?> type) {
        return registry.getID(type);
    }

}

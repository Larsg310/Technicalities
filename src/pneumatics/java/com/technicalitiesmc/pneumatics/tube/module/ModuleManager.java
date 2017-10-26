package com.technicalitiesmc.pneumatics.tube.module;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.technicalitiesmc.api.pneumatics.TubeModule;
import com.technicalitiesmc.pneumatics.TKPneumatics;
import com.technicalitiesmc.util.simple.SimpleRegistry;
import net.minecraftforge.registries.ForgeRegistry;

import java.util.Set;

public enum ModuleManager {
    INSTANCE;

    @SimpleRegistry(value = TKPneumatics.MODID + ":tube_module", type = TubeModule.RegistryEntry.class)
    public static ForgeRegistry<TubeModule.RegistryEntry> registry;
    private static BiMap<TubeModule.RegistryEntry, TubeModule.Type<?>> map;

    public Set<TubeModule.Type<?>> getModuleTypes() {
        initMap();
        return map.values();
    }

    public TubeModule.Type get(int id) {
        initMap();
        return map.get(registry.getValue(id));
    }

    public TubeModule.RegistryEntry getRegistryEntry(int id) {
        return registry.getValue(id);
    }

    public int getID(TubeModule.Type type) {
        initMap();
        return registry.getID(map.inverse().get(type));
    }

    private void initMap() {
        if (map == null) {
            map = HashBiMap.create();
            registry.forEach(e -> map.put(e, e.getType()));
        }
    }

}

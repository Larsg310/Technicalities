package com.technicalitiesmc.pneumatics.init;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.base.TKBase;
import com.technicalitiesmc.pneumatics.item.ItemTubeModule;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;

public class TKPneumaticsItems {

    public static Item tube_module;

    public static void initialize() {
        tube_module = new ItemTubeModule();
    }

    public static void register() {
        Technicalities.register(tube_module.setRegistryName("tube_module")).accept(() -> TKBase.proxy.registerItemModel(tube_module, 0,
                new ModelResourceLocation(tube_module.getRegistryName(), "inventory")));
    }

}

package com.technicalitiesmc.mechanical.init;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.base.TKBase;
import com.technicalitiesmc.mechanical.block.BlockKineticTest;
import com.technicalitiesmc.mechanical.block.BlockShaft;
import com.technicalitiesmc.mechanical.tile.TileKineticTest;
import com.technicalitiesmc.mechanical.tile.TileShaft;
import com.technicalitiesmc.util.item.ItemBlockBase;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class TKMechanicalBlocks {

    public static Block shaft;

    public static Block test;

    public static void initialize() {
        shaft = new BlockShaft();

        test = new BlockKineticTest();
    }

    public static void register() {
        Technicalities.register(shaft.setRegistryName("shaft"));
        Technicalities.register(new ItemBlockBase(shaft))
                .accept(() -> TKBase.proxy.registerItemModel(shaft, 0, new ModelResourceLocation(shaft.getRegistryName(), "inventory")));
        GameRegistry.registerTileEntity(TileShaft.class, "shaft");

        Technicalities.register(test.setRegistryName("kinetic_test"));
        Technicalities.register(new ItemBlockBase(test))
                .accept(() -> TKBase.proxy.registerItemModel(test, 0, new ModelResourceLocation(test.getRegistryName(), "inventory")));
        GameRegistry.registerTileEntity(TileKineticTest.class, "kinetic_test");
    }

}

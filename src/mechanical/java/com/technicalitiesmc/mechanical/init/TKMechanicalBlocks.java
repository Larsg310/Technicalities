package com.technicalitiesmc.mechanical.init;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.base.TKBase;
import com.technicalitiesmc.mechanical.block.BlockConveyor;
import com.technicalitiesmc.mechanical.block.BlockConveyorSmall;
import com.technicalitiesmc.mechanical.block.BlockGrate;
import com.technicalitiesmc.mechanical.block.BlockKineticTest;
import com.technicalitiesmc.mechanical.block.BlockShaft;
import com.technicalitiesmc.mechanical.tile.TileConveyorSmall;
import com.technicalitiesmc.mechanical.tile.TileKineticTest;
import com.technicalitiesmc.mechanical.tile.TileShaft;
import com.technicalitiesmc.util.item.ItemBlockBase;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class TKMechanicalBlocks {

    public static Block shaft;
    public static Block conveyor;
    public static Block conveyor_small;
    public static Block grate;

    public static Block test;

    public static void initialize() {
        shaft = new BlockShaft();
        conveyor = new BlockConveyor();
        conveyor_small = new BlockConveyorSmall();
        grate = new BlockGrate();

        test = new BlockKineticTest();
    }

    public static void register() {
        Technicalities.register(shaft.setRegistryName("shaft"));
        Technicalities.register(new ItemBlockBase(shaft))
                .accept(() -> TKBase.proxy.registerItemModel(shaft, 0, new ModelResourceLocation(shaft.getRegistryName(), "inventory")));
        GameRegistry.registerTileEntity(TileShaft.class, "shaft");

        Technicalities.register(conveyor.setRegistryName("conveyor"));
        Technicalities.register(new ItemBlockBase(conveyor)).accept(
                () -> TKBase.proxy.registerItemModel(conveyor, 0, new ModelResourceLocation(conveyor.getRegistryName(), "inventory")));

        Technicalities.register(conveyor_small.setRegistryName("conveyor_small"));
        Technicalities.register(new ItemBlockBase(conveyor_small)).accept(() -> TKBase.proxy.registerItemModel(conveyor_small, 0,
                new ModelResourceLocation(conveyor_small.getRegistryName(), "inventory")));
        GameRegistry.registerTileEntity(TileConveyorSmall.class, "conveyor_small");

        Technicalities.register(grate.setRegistryName("grate"));
        Technicalities.register(new ItemBlockBase(grate))
                .accept(() -> TKBase.proxy.registerItemModel(grate, 0, new ModelResourceLocation(grate.getRegistryName(), "inventory")));

        Technicalities.register(test.setRegistryName("kinetic_test"));
        Technicalities.register(new ItemBlockBase(test))
                .accept(() -> TKBase.proxy.registerItemModel(test, 0, new ModelResourceLocation(test.getRegistryName(), "inventory")));
        GameRegistry.registerTileEntity(TileKineticTest.class, "kinetic_test");
    }

}

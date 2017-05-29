package com.technicalitiesmc.base.init;

import com.technicalitiesmc.base.TKBase;
import com.technicalitiesmc.base.block.BlockBarrel;
import com.technicalitiesmc.base.block.BlockCrate;
import com.technicalitiesmc.base.tile.TileBarrel;
import com.technicalitiesmc.base.tile.TileCrate;
import com.technicalitiesmc.lib.item.ItemBlockBase;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class TKBaseBlocks {

    public static Block crate;
    public static Block barrel;

    public static void initialize() {
        crate = new BlockCrate();
        barrel = new BlockBarrel();
    }

    public static void register() {
        GameRegistry.register(crate.setRegistryName("crate"));
        GameRegistry.register(new ItemBlockBase(crate));
        GameRegistry.registerTileEntity(TileCrate.class, "crate");
        TKBase.proxy.registerItemModel(crate, 0, new ModelResourceLocation(crate.getRegistryName(), "inventory"));

        GameRegistry.register(barrel.setRegistryName("barrel"));
        GameRegistry.register(new ItemBlockBase(barrel));
        GameRegistry.registerTileEntity(TileBarrel.class, "barrel");
        TKBase.proxy.registerItemModel(barrel, 0, new ModelResourceLocation(barrel.getRegistryName(), "inventory"));
    }

}

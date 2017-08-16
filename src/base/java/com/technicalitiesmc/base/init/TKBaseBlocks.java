package com.technicalitiesmc.base.init;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.base.TKBase;
import com.technicalitiesmc.base.block.BlockBarrel;
import com.technicalitiesmc.base.block.BlockCraftingSlab;
import com.technicalitiesmc.base.block.BlockCrate;
import com.technicalitiesmc.base.block.BlockWorkbench;
import com.technicalitiesmc.base.tile.TileBarrel;
import com.technicalitiesmc.base.tile.TileCraftingSlab;
import com.technicalitiesmc.base.tile.TileCrate;
import com.technicalitiesmc.base.tile.TileWorkbench;
import com.technicalitiesmc.lib.item.ItemBlockBase;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class TKBaseBlocks {

    public static Block crate;
    public static Block barrel;
    public static Block crafting_slab;
    public static Block workbench;

    public static void initialize() {
        crate = new BlockCrate();
        barrel = new BlockBarrel();
        crafting_slab = new BlockCraftingSlab();
        workbench = new BlockWorkbench();
    }

    public static void register() {
        Technicalities.register(crate.setRegistryName("crate"));
        Technicalities.register(new ItemBlockBase(crate))
                .accept(() -> TKBase.proxy.registerItemModel(crate, 0, new ModelResourceLocation(crate.getRegistryName(), "inventory")));
        GameRegistry.registerTileEntity(TileCrate.class, "crate");

        Technicalities.register(barrel.setRegistryName("barrel"));
        Technicalities.register(new ItemBlockBase(barrel))
                .accept(() -> TKBase.proxy.registerItemModel(barrel, 0, new ModelResourceLocation(barrel.getRegistryName(), "inventory")));
        GameRegistry.registerTileEntity(TileBarrel.class, "barrel");

        Technicalities.register(crafting_slab.setRegistryName("crafting_slab"));
        Technicalities.register(new ItemBlockBase(crafting_slab)).accept(() -> TKBase.proxy.registerItemModel(crafting_slab, 0,
                new ModelResourceLocation(crafting_slab.getRegistryName(), "inventory")));
        GameRegistry.registerTileEntity(TileCraftingSlab.class, "crafting_slab");

        Technicalities.register(workbench.setRegistryName("workbench"));
        Technicalities.register(new ItemBlockBase(workbench)).accept(
                () -> TKBase.proxy.registerItemModel(workbench, 0, new ModelResourceLocation(workbench.getRegistryName(), "inventory")));
        GameRegistry.registerTileEntity(TileWorkbench.class, "workbench");
    }

}

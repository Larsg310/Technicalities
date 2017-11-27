package com.technicalitiesmc.base.init;

import com.technicalitiesmc.base.Technicalities;
import com.technicalitiesmc.base.block.*;
import com.technicalitiesmc.base.tile.*;
import com.technicalitiesmc.lib.item.ItemBlockBase;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = Technicalities.MODID)
public class TKBaseBlocks {

    public static Block crate = new BlockCrate();
    public static Block barrel = new BlockBarrel();
    public static Block crafting_slab = new BlockCraftingSlab();
    public static Block workbench = new BlockWorkbench();

    public static Block channel = new BlockChannel();

    @SubscribeEvent
    public static void onBlockRegistration(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();

        register(registry, crate, "crate", TileCrate.class);
        register(registry, barrel, "barrel", TileBarrel.class);
        register(registry, crafting_slab, "crafting_slab", TileCraftingSlab.class);
        register(registry, workbench, "workbench", TileWorkbench.class);
        register(registry, channel, "channel", TileChannel.class);
    }

    @SubscribeEvent
    public static void onItemRegistration(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();

        registerItem(registry, crate);
        registerItem(registry, barrel);
        registerItem(registry, crafting_slab);
        registerItem(registry, workbench);
        registerItem(registry, channel);
    }

    private static void register(IForgeRegistry<Block> registry, Block block, String name) {
        register(registry, block, name, null);
    }

    private static void register(IForgeRegistry<Block> registry, Block block, String name, Class<? extends TileEntity> tile) {
        ResourceLocation resLoc = new ResourceLocation(Technicalities.MODID, name);
        registry.register(block.setRegistryName(resLoc));
        if (tile != null) {
            GameRegistry.registerTileEntity(tile, resLoc.toString());
        }
    }

    private static void registerItem(IForgeRegistry<Item> registry, Block block) {
        registerItem(registry, new ItemBlockBase(block), block.getRegistryName().getResourcePath());
    }

    private static void registerItem(IForgeRegistry<Item> registry, Item item, String name) {
        ResourceLocation resLoc = new ResourceLocation(Technicalities.MODID, name);
        registry.register(item.setRegistryName(resLoc));
        Technicalities.proxy.registerItemModel(item, 0, new ModelResourceLocation(resLoc, "inventory"));
    }

}

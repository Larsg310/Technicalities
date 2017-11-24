package com.technicalitiesmc.pneumatics.init;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.pneumatics.TKPneumatics;
import com.technicalitiesmc.pneumatics.block.BlockPneumaticTube;
import com.technicalitiesmc.pneumatics.tile.TilePneumaticTubeClient;
import com.technicalitiesmc.pneumatics.tile.TilePneumaticTubeServer;
import com.technicalitiesmc.util.item.ItemBlockBase;
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

@Mod.EventBusSubscriber(modid = TKPneumatics.MODID)
public class TKPneumaticsBlocks {

    public static Block pneumatic_tube = new BlockPneumaticTube();

    @SubscribeEvent
    public static void onBlockRegistration(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();

        register(registry, pneumatic_tube, "pneumatic_tube", TilePneumaticTubeServer.class);
        GameRegistry.registerTileEntity(TilePneumaticTubeClient.class, "pneumatic_tube.client");
    }

    @SubscribeEvent
    public static void onItemRegistration(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();

        registerItem(registry, pneumatic_tube);
    }

    private static void register(IForgeRegistry<Block> registry, Block block, String name) {
        register(registry, block, name, null);
    }

    private static void register(IForgeRegistry<Block> registry, Block block, String name, Class<? extends TileEntity> tile) {
        ResourceLocation resLoc = new ResourceLocation(TKPneumatics.MODID, name);
        registry.register(block.setRegistryName(resLoc));
        if (tile != null) {
            GameRegistry.registerTileEntity(tile, resLoc.toString());
        }
    }

    private static void registerItem(IForgeRegistry<Item> registry, Block block) {
        registerItem(registry, new ItemBlockBase(block), block.getRegistryName().getResourcePath());
    }

    private static void registerItem(IForgeRegistry<Item> registry, Item item, String name) {
        ResourceLocation resLoc = new ResourceLocation(TKPneumatics.MODID, name);
        registry.register(item.setRegistryName(resLoc));
        Technicalities.proxy.registerItemModel(item, 0, new ModelResourceLocation(resLoc, "inventory"));
    }

}

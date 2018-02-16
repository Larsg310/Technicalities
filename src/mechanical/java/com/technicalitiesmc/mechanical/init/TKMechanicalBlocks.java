package com.technicalitiesmc.mechanical.init;

import com.technicalitiesmc.base.Technicalities;
import com.technicalitiesmc.lib.item.ItemBlockBase;
import com.technicalitiesmc.mechanical.TKMechanical;
import com.technicalitiesmc.mechanical.block.BlockFlywheel;
import com.technicalitiesmc.mechanical.block.BlockGear;
import com.technicalitiesmc.mechanical.block.BlockHandCrank;
import com.technicalitiesmc.mechanical.block.BlockKineticTest;
import com.technicalitiesmc.mechanical.block.BlockShaft;
import com.technicalitiesmc.mechanical.tile.TileFlywheel;
import com.technicalitiesmc.mechanical.tile.TileGear;
import com.technicalitiesmc.mechanical.tile.TileHandCrank;
import com.technicalitiesmc.mechanical.tile.TileKineticTest;
import com.technicalitiesmc.mechanical.tile.TileShaft;
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

@Mod.EventBusSubscriber(modid = TKMechanical.MODID)
public class TKMechanicalBlocks {
    public static Block shaft = new BlockShaft();
    public static Block flywheel = new BlockFlywheel();
    public static Block hand_crank = new BlockHandCrank();
    public static Block gear = new BlockGear();

    public static Block test = new BlockKineticTest();

    @SubscribeEvent
    public static void onBlockRegistration(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();

        register(registry, shaft, "shaft", TileShaft.class);
        register(registry, flywheel, "flywheel", TileFlywheel.class);
        register(registry, hand_crank, "hand_crank", TileHandCrank.class);
        register(registry, gear, "gear", TileGear.class);
        register(registry, test, "test", TileKineticTest.class);
    }

    @SubscribeEvent
    public static void onItemRegistration(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();

        registerItem(registry, shaft);
        registerItem(registry, flywheel);
        registerItem(registry, hand_crank);
        registerItem(registry, gear);
        registerItem(registry, test);
    }

    private static void register(IForgeRegistry<Block> registry, Block block, String name) {
        register(registry, block, name, null);
    }

    private static void register(IForgeRegistry<Block> registry, Block block, String name, Class<? extends TileEntity> tile) {
        ResourceLocation resLoc = new ResourceLocation(TKMechanical.MODID, name);
        registry.register(block.setRegistryName(resLoc));
        if (tile != null) {
            GameRegistry.registerTileEntity(tile, resLoc.toString());
        }
    }

    private static void registerItem(IForgeRegistry<Item> registry, Block block) {
        registerItem(registry, new ItemBlockBase(block), block.getRegistryName().getResourcePath());
    }

    private static void registerItem(IForgeRegistry<Item> registry, Item item, String name) {
        ResourceLocation resLoc = new ResourceLocation(TKMechanical.MODID, name);
        registry.register(item.setRegistryName(resLoc));
        Technicalities.proxy.registerItemModel(item, 0, new ModelResourceLocation(resLoc, "inventory"));
    }
}

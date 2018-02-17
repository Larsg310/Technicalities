package com.technicalitiesmc.mechanical.init;

import com.technicalitiesmc.base.Technicalities;
import com.technicalitiesmc.mechanical.TKMechanical;
import com.technicalitiesmc.mechanical.item.ItemDisk;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = TKMechanical.MODID)
public class TKMechanicalItems {
    public static Item stone_disk = new ItemDisk(20.0f);

    @SubscribeEvent
    public static void onItemRegistration(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();

        register(registry, stone_disk, "stone_disk");
    }

    private static void register(IForgeRegistry<Item> registry, Item item, String name) {
        ResourceLocation resLoc = new ResourceLocation(TKMechanical.MODID, name);
        registry.register(item.setRegistryName(resLoc));
        Technicalities.proxy.registerItemModel(item, 0, new ModelResourceLocation(resLoc, "inventory"));
    }
}

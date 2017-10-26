package com.technicalitiesmc.pneumatics.init;

import com.technicalitiesmc.base.TKBase;
import com.technicalitiesmc.pneumatics.TKPneumatics;
import com.technicalitiesmc.pneumatics.item.ItemTubeModule;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = TKPneumatics.MODID)
public class TKPneumaticsItems {

    public static Item tube_module = new ItemTubeModule();

    @SubscribeEvent
    public static void onItemRegistration(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();

        register(registry, tube_module, "tube_module");
    }

    private static void register(IForgeRegistry<Item> registry, Item item, String name){
        ResourceLocation resLoc = new ResourceLocation(TKPneumatics.MODID, name);
        registry.register(item.setRegistryName(resLoc));
        TKBase.proxy.registerItemModel(item, 0, new ModelResourceLocation(resLoc, "inventory"));
    }

}

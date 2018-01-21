package com.technicalitiesmc.base.init;

import com.technicalitiesmc.base.Technicalities;
import com.technicalitiesmc.base.item.ItemBookManual;
import com.technicalitiesmc.base.item.ItemHeatProbe;
import com.technicalitiesmc.base.item.ItemRecipeBook;
import com.technicalitiesmc.lib.item.ItemBase;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = Technicalities.MODID)
public class TKBaseItems {

    public static Item reed_stick = new ItemBase();
    public static Item recipe_book = new ItemRecipeBook();
    public static Item book_manual = new ItemBookManual();

    private static Item heatProbe = new ItemHeatProbe();

    @SubscribeEvent
    public static void onItemRegistration(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();

        register(registry, reed_stick, "reed_stick");
        register(registry, recipe_book, "recipe_book");
        register(registry, book_manual, "book_manual");

        register(registry, heatProbe, "heat_probe");

        // TODO: does this belong here? Please move if necessary.
        OreDictionary.registerOre("book", book_manual);
    }

    private static void register(IForgeRegistry<Item> registry, Item item, String name){
        ResourceLocation resLoc = new ResourceLocation(Technicalities.MODID, name);
        registry.register(item.setRegistryName(resLoc));
        Technicalities.proxy.registerItemModel(item, 0, new ModelResourceLocation(resLoc, "inventory"));
    }

}

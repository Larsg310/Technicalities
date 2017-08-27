package com.technicalitiesmc.base.init;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.base.TKBase;
import com.technicalitiesmc.base.item.ItemRecipeBook;
import com.technicalitiesmc.util.item.ItemBase;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;

public class TKBaseItems {

    public static Item reed_stick;
    public static Item recipe_book;

    public static void initialize() {
        reed_stick = new ItemBase();
        recipe_book = new ItemRecipeBook();
    }

    public static void register() {
        Technicalities.register(reed_stick.setRegistryName("reed_stick")).accept(
                () -> TKBase.proxy.registerItemModel(reed_stick, 0, new ModelResourceLocation(reed_stick.getRegistryName(), "inventory")));
        Technicalities.register(recipe_book.setRegistryName("recipe_book")).accept(() -> TKBase.proxy.registerItemModel(recipe_book, 0,
                new ModelResourceLocation(recipe_book.getRegistryName(), "inventory")));
    }

}

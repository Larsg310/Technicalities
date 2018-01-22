package com.technicalitiesmc.base.init;

import net.minecraftforge.oredict.OreDictionary;

public class OreDictRegister {
    private OreDictRegister() {}

    public static void registerItems() {
        OreDictionary.registerOre("book", TKBaseItems.book_manual);
    }
}

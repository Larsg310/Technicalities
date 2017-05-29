package com.technicalitiesmc.base;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;

public class TKBaseCommonProxy {

    public void registerItemModel(Item item, int meta, ModelResourceLocation location) {
    }

    public void registerItemModel(Block block, int meta, ModelResourceLocation location) {
        registerItemModel(Item.getItemFromBlock(block), meta, location);
    }

}

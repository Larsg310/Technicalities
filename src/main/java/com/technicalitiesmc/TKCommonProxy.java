package com.technicalitiesmc;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.discovery.ASMDataTable;

import java.io.IOException;
import java.io.InputStream;

public class TKCommonProxy {

    public void preInit() {

    }

    public void registerItemModel(Item item, int meta, ModelResourceLocation location) {
    }

    public void registerItemModel(Block block, int meta, ModelResourceLocation location) {
        registerItemModel(Item.getItemFromBlock(block), meta, location);
    }

    public void bindSpecialRenderers(ASMDataTable asmDataTable) {
    }

    public World getWorld() {
        return null;
    }

    public boolean isGamePaused() {
        return false;
    }

    public InputStream readResource(ResourceLocation path) throws IOException {
        return null;
    }

}

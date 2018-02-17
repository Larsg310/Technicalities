package com.technicalitiesmc.base.proxies;

import com.technicalitiesmc.base.init.OreDictRegister;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;
import java.io.InputStream;

public class TKCommonProxy {
    public void preInit() {
    }

    public void init() {
        OreDictRegister.registerItems();
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

    public void schedule(Side side, Runnable task) {
        if (side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(task);
        }
    }

}

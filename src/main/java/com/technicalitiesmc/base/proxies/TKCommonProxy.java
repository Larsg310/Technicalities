package com.technicalitiesmc.base.proxies;

import elec332.core.inventory.window.IWindowFactory;
import elec332.core.inventory.window.IWindowHandler;
import elec332.core.inventory.window.Window;
import elec332.core.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.discovery.ASMDataTable;

import java.io.IOException;
import java.io.InputStream;

public class TKCommonProxy implements IWindowHandler {

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

    @Override
    public Window createWindow(byte ID, EntityPlayer entityPlayer, World world, int x, int y, int z) {
        final TileEntity tile = WorldHelper.getTileAt(world, new BlockPos(x, y, z));
        switch (ID){
            //eh
            default:
                if (tile instanceof IWindowFactory) {
                    return ((IWindowFactory) tile).createWindow();
                }
                return null;
        }

    }

}

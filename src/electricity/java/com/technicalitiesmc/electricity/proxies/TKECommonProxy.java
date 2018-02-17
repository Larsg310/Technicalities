package com.technicalitiesmc.electricity.proxies;

import elec332.core.inventory.window.IWindowFactory;
import elec332.core.inventory.window.IWindowHandler;
import elec332.core.inventory.window.Window;
import elec332.core.world.WorldHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by Elec332 on 23-11-2017.
 */
public class TKECommonProxy implements IWindowHandler {

    public void initRendering() {
    }

    @Override
    public Window createWindow(byte ID, EntityPlayer entityPlayer, World world, int x, int y, int z) {
        final TileEntity tile = WorldHelper.getTileAt(world, new BlockPos(x, y, z));
        switch (ID) {
            //eh
            default:
                if (tile instanceof IWindowFactory) {
                    return ((IWindowFactory) tile).createWindow();
                }
                return null;
        }

    }

}

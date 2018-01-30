package com.technicalitiesmc.base.network;

import com.technicalitiesmc.base.manual.client.gui.GuiManual;
import elec332.core.inventory.window.IWindowFactory;
import elec332.core.inventory.window.IWindowHandler;
import elec332.core.inventory.window.Window;
import elec332.core.world.WorldHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public final class TKGuiHandler implements IGuiHandler, IWindowHandler {
    public static final TKGuiHandler instance = new TKGuiHandler();

    private TKGuiHandler() {}

    public enum GuiId {
        BOOK_MANUAL;

        public static final GuiId[] VALUES = values();
    }

    @Override
    @Nullable
    public Object getServerGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        return null;
    }

    @Override
    @Nullable
    public Object getClientGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        switch (GuiId.VALUES[id]) {
            case BOOK_MANUAL:
                return new GuiManual();
        }
        return null;
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

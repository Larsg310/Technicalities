package com.technicalitiesmc.pneumatics.network;

import com.technicalitiesmc.api.pneumatics.TubeModule;
import com.technicalitiesmc.pneumatics.tile.TilePneumaticTubeBase;
import com.technicalitiesmc.util.network.GuiHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TKPGuiHandler implements GuiHandler.IHandler {

    @Override
    public Container getContainer(World world, BlockPos pos, EntityPlayer player, int id) {
        TilePneumaticTubeBase te = (TilePneumaticTubeBase) world.getTileEntity(pos);
        TubeModule module = te.getModule(EnumFacing.getFront(id));
        if (module != null && module instanceof TubeModule.ContainerModule) {
            return ((TubeModule.ContainerModule) module).createContainer(player);
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getGUI(World world, BlockPos pos, EntityPlayer player, int id) {
        TilePneumaticTubeBase te = (TilePneumaticTubeBase) world.getTileEntity(pos);
        TubeModule module = te.getModule(EnumFacing.getFront(id));
        if (module != null && module instanceof TubeModule.ContainerModule) {
            return ((TubeModule.ContainerModule) module).createGUI(player);
        }
        return null;
    }

}

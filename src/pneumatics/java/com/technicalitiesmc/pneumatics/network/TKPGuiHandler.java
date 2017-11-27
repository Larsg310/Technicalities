package com.technicalitiesmc.pneumatics.network;

import com.technicalitiesmc.api.pneumatics.TubeModule;
import com.technicalitiesmc.pneumatics.tile.TilePneumaticTubeBase;
import com.technicalitiesmc.pneumatics.tube.IWindowModule;
import elec332.core.inventory.window.IWindowHandler;
import elec332.core.inventory.window.Window;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class TKPGuiHandler implements IGuiHandler, IWindowHandler {

    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TilePneumaticTubeBase te = (TilePneumaticTubeBase) world.getTileEntity(new BlockPos(x, y, z));
        if (te != null) {
            TubeModule module = te.getModule(EnumFacing.getFront(ID));
            if (module != null && module instanceof TubeModule.ContainerModule) {
                return ((TubeModule.ContainerModule) module).createContainer(player);
            }
        }
        return null;
    }

    @Nullable
    @Override
    @SideOnly(Side.CLIENT)
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TilePneumaticTubeBase te = (TilePneumaticTubeBase) world.getTileEntity(new BlockPos(x, y, z));
        if (te != null) {
            TubeModule module = te.getModule(EnumFacing.getFront(ID));
            if (module != null && module instanceof TubeModule.ContainerModule) {
                return ((TubeModule.ContainerModule) module).createGUI(player);
            }
        }
        return null;
    }

    @Override
    public Window createWindow(byte b, EntityPlayer entityPlayer, World world, int x, int y, int z) {
        TilePneumaticTubeBase te = (TilePneumaticTubeBase) world.getTileEntity(new BlockPos(x, y, z));
        if (te != null) {
            TubeModule module = te.getModule(EnumFacing.getFront(b));
            if (module != null && module instanceof IWindowModule) {
                return ((IWindowModule) module).createWindow();
            }
        }
        return null;
    }

}

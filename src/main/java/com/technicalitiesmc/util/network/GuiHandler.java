package com.technicalitiesmc.util.network;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class GuiHandler implements IGuiHandler {

    private final List<IHandler> handlers = new ArrayList<>();
    private final Multimap<Block, IHandler> blockHandlers = MultimapBuilder.hashKeys().arrayListValues().build();

    public void add(IHandler handler) {
        handlers.add(handler);
    }

    public void add(Block block, IHandler handler) {
        blockHandlers.put(block, handler);
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        Block block = world.getBlockState(pos).getBlock();
        for (IHandler handler : blockHandlers.get(block)) {
            Object element = handler.getContainer(world, pos, player, id);
            if (element != null) {
                return element;
            }
        }
        for (IHandler handler : handlers) {
            Object element = handler.getContainer(world, pos, player, id);
            if (element != null) {
                return element;
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        Block block = world.getBlockState(pos).getBlock();
        for (IHandler handler : blockHandlers.get(block)) {
            Object element = handler.getGUI(world, pos, player, id);
            if (element != null) {
                return element;
            }
        }
        for (IHandler handler : handlers) {
            Object element = handler.getGUI(world, pos, player, id);
            if (element != null) {
                return element;
            }
        }
        return null;
    }

    public interface IHandler {

        public Container getContainer(World world, BlockPos pos, EntityPlayer player, int id);

        @SideOnly(Side.CLIENT)
        public GuiScreen getGUI(World world, BlockPos pos, EntityPlayer player, int id);

    }

}

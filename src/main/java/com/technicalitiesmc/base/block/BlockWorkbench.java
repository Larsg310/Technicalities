package com.technicalitiesmc.base.block;

import com.google.common.base.Preconditions;
import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.base.client.gui.GuiWorkbench;
import com.technicalitiesmc.base.item.ItemRecipeBook;
import com.technicalitiesmc.base.item.ItemRecipeBook.Recipe;
import com.technicalitiesmc.base.tile.TileWorkbench;
import com.technicalitiesmc.util.block.BlockBase;
import com.technicalitiesmc.util.inventory.InsertingSlotItemHandler;
import com.technicalitiesmc.util.inventory.SimpleContainer;
import com.technicalitiesmc.util.inventory.SimpleItemHandler;
import com.technicalitiesmc.util.network.GuiHandler;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.SlotItemHandler;

public class BlockWorkbench extends BlockBase implements ITileEntityProvider {

    public BlockWorkbench() {
        super(Material.ROCK);
        Technicalities.guiHandler.add(this, new WorkbenchGuiHandler());
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileWorkbench();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, RayTraceResult hit) {
        if (!world.isRemote) {
            player.openGui(Technicalities.MODID, 0, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public boolean eventReceived(IBlockState state, World world, BlockPos pos, int id, int param) {
        if (!world.isRemote && id == -1) {
            TileWorkbench te = Preconditions.checkNotNull((TileWorkbench) world.getTileEntity(pos));
            if (param == -1) {
                SimpleItemHandler inv = te.getInventory();
                ItemStack book = inv.getStackInSlot(TileWorkbench.BOOK_START).copy();
                if (!book.isEmpty() && te.consumeInk(true) && ItemRecipeBook.addRecipe(book, Recipe.fromGrid(te.getGrid(), world))) {
                    inv.setStackInSlot(TileWorkbench.BOOK_START, book);
                    te.consumeInk(false);
                }
            } else if (param >= 100) {
                SimpleItemHandler inv = te.getInventory();
                ItemStack book = inv.getStackInSlot(TileWorkbench.BOOK_START).copy();
                ItemRecipeBook.removeRecipe(book, param - 100);
                inv.setStackInSlot(TileWorkbench.BOOK_START, book);
            } else if (param >= 0 && param < 32) {
                boolean pulledAll = true;
                for (int i = 0; i < 9; i++) {
                    ItemStack stack = te.getCraftingGrid().getStackInSlot(i);
                    ItemStack leftover = ItemHandlerHelper.insertItemStacked(te.getExposedInventory(), stack, false);
                    if (!leftover.isEmpty()) {
                        pulledAll = false;
                    }
                    te.getCraftingGrid().setStackInSlot(i, leftover);
                }

                if (pulledAll) {
                    Recipe recipe = te.getRecipes().get(param).getKey();
                    ItemStack[] grid = recipe.getGrid();
                    for (int i = 0; i < 9; i++) {
                        te.pullStack(grid[i]);
                        te.getCraftingGrid().setStackInSlot(i, grid[i].copy());
                    }
                }
            }
        }
        return false;
    }

    private class WorkbenchGuiHandler implements GuiHandler.IHandler {

        @Override
        public Container getContainer(World world, BlockPos pos, EntityPlayer player, int id) {
            TileWorkbench te = (TileWorkbench) world.getTileEntity(pos);
            IItemHandler inventory = te.getInventory();
            IItemHandler grid = te.getCraftingGrid();

            SimpleContainer container = new SimpleContainer() {

                @Override
                public boolean canMergeSlot(ItemStack stack, Slot slot) {
                    if (slot instanceof SlotItemHandler && ((SlotItemHandler) slot).getItemHandler() == grid && slot.getSlotIndex() == 9) {
                        return false;
                    }
                    return super.canMergeSlot(stack, slot);
                }
            };

            container.addSlotToContainer(new SlotItemHandler(grid, 9, 125, 36) {

                @Override
                public boolean isItemValid(ItemStack stack) {
                    return false;
                }

            });
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 3; ++j) {
                    container.addSlotToContainer(new SlotItemHandler(grid, j + i * 3, 31 + j * 18, 18 + i * 18));
                }
            }

            container.addSlotToContainer(new InsertingSlotItemHandler(inventory, TileWorkbench.BOOK_START, 8, 36));
            container.addSlotToContainer(new InsertingSlotItemHandler(inventory, TileWorkbench.BOOK_START + 1, 152, 54));

            for (int i = 0; i < 2; ++i) {
                for (int j = 0; j < 9; ++j) {
                    container.addSlotToContainer(
                            new InsertingSlotItemHandler(inventory, TileWorkbench.INV_START + j + i * 9, 8 + j * 18, 77 + i * 18));
                }
            }

            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 9; ++j) {
                    container.addSlotToContainer(new Slot(player.inventory, j + i * 9 + 9, 8 + j * 18, 127 + i * 18));
                }
            }
            for (int k = 0; k < 9; ++k) {
                container.addSlotToContainer(new Slot(player.inventory, k, 8 + k * 18, 127 + 58));
            }

            return container;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public GuiScreen getGUI(World world, BlockPos pos, EntityPlayer player, int id) {
            return new GuiWorkbench(getContainer(world, pos, player, id), (TileWorkbench) world.getTileEntity(pos));
        }

    }

}

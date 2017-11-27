package com.technicalitiesmc.base.inventory;

import com.technicalitiesmc.base.Technicalities;
import com.technicalitiesmc.base.item.ItemRecipeBook;
import com.technicalitiesmc.base.tile.TileWorkbench;
import com.technicalitiesmc.lib.inventory.SimpleItemHandler;
import com.technicalitiesmc.lib.stack.StackList;
import elec332.core.client.RenderHelper;
import elec332.core.client.util.GuiDraw;
import elec332.core.inventory.tooltip.ToolTip;
import elec332.core.inventory.widget.Widget;
import elec332.core.inventory.window.Window;
import elec332.core.main.ElecCore;
import elec332.core.util.InventoryHelper;
import elec332.core.util.NBTHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Created by Elec332 on 27-11-2017.
 */
public class WidgetRecipeBook extends Widget {

    public WidgetRecipeBook(TileWorkbench tile, int slot) {
        super(-129, 0, 0, 0, 129, 209);
        this.slot = slot;
        this.workbench = tile;
    }

    private TileWorkbench workbench;
    private int slot;

    @Override
    public void draw(Window window, int guiX, int guiY, int mouseX, int mouseY) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(guiX, guiY, 0);
        if (!workbench.getInventory().getStackInSlot(slot).isEmpty()) {
            RenderHelper.bindTexture(new ResourceLocation(Technicalities.MODID, "textures/gui/workbench_book.png"));
            GuiDraw.drawTexturedModalRect(-129, 0, 0, 0, 129, window.ySize);

            int index = 0;
            for (Pair<ItemRecipeBook.Recipe, StackList> pair : workbench.getRecipes()) {
                int x = -110 + (index % 4) * 24, y = 15 + (index / 4) * 22;
                if (!pair.getValue().isEmpty()) {
                    GuiDraw.drawRect(x, y, x + 20, y + 20, 0x7FFF4030);
                }
                if (isMouseOver(mouseX, mouseY, x, y, 20, 20)) {
                    GuiDraw.drawRect(x, y, x + 20, y + 20, 0x40302010);
                }
                index++;
            }

            RenderHelper.enableGUIStandardItemLighting();

            index = 0;
            for (Pair<ItemRecipeBook.Recipe, StackList> pair : workbench.getRecipes()) {
                RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
                int x = -108 + (index % 4) * 24, y = 17 + (index / 4) * 22;
                itemRender.renderItemIntoGUI(pair.getKey().getResult(), x, y);
                if (isMouseOver(mouseX, mouseY, x - 2, y - 2, 20, 20)) {
                    itemRender.renderItemOverlayIntoGUI(RenderHelper.getMCFontrenderer(), pair.getKey().getResult(), x, y, pair.getKey().getResult().getCount() + "");
                }
                index++;
            }

            RenderHelper.disableStandardItemLighting();
        }
        RenderHelper.bindTexture(new ResourceLocation(Technicalities.MODID, "textures/gui/workbench.png"));

        // Book icon
        GuiDraw.drawTexturedModalRect(152, 17, window.xSize, 0, 16, 16);

        // Ink meter
        int inkAmt = workbench.getInk();
        GuiDraw.drawTexturedModalRect(152, 35 + 15 - inkAmt, window.xSize, 16 + 15 - inkAmt, 16, 16);

        GlStateManager.popMatrix();
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        int index = 0;
        for (Pair<ItemRecipeBook.Recipe, StackList> pair : workbench.getRecipes()) {
            int x = -110 + (index % 4) * 24, y = 15 + (index / 4) * 22;
            if (pair.getValue().isEmpty() && isMouseOver(mouseX, mouseY, x, y, 20, 20)) {
                int send = -1;
                if (button == 0) {
                    send = index;
                } else if (button == 2 && GuiScreen.isShiftKeyDown() && GuiScreen.isCtrlKeyDown()) {
                    send = index + 100;
                }
                if (send != -10) {
                    sendNBTChangesToServer(new NBTHelper().addToTag(send, "sids").serializeNBT());
                    return true;
                }
            }
            index++;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void readNBTChangesFromPacketServerSide(NBTTagCompound tagCompound) {
        int param = tagCompound.getInteger("sids");
        if (param >= 100) {
            SimpleItemHandler inv = workbench.getInventory();
            ItemStack book = inv.getStackInSlot(TileWorkbench.BOOK_START).copy();
            ItemRecipeBook.removeRecipe(book, param - 100);
            inv.setStackInSlot(TileWorkbench.BOOK_START, book);
        } else if (param >= 0 && param < 32) {
            boolean pulledAll = true;
            for (int i = 0; i < 9; i++) {
                ItemStack stack = workbench.getCraftingGrid().getStackInSlot(i);
                ItemStack leftover = ItemHandlerHelper.insertItemStacked(workbench.getExposedInventory(), stack, false);
                if (!leftover.isEmpty()) {
                    pulledAll = false;
                }
                workbench.getCraftingGrid().setStackInSlot(i, leftover);
            }

            if (pulledAll) {
                ItemRecipeBook.Recipe recipe = workbench.getRecipes().get(param).getKey();
                ItemStack[] grid = recipe.getGrid();
                for (int i = 0; i < 9; i++) {
                    workbench.pullStack(grid[i]);
                    workbench.getCraftingGrid().setStackInSlot(i, grid[i].copy());
                }
            }
        }
    }

    @Override
    public ToolTip getToolTip(int mouseX, int mouseY) {
        int index = 0;
        for (Pair<ItemRecipeBook.Recipe, StackList> pair : workbench.getRecipes()) {
            int x = -110 + (index % 4) * 24, y = 15 + (index / 4) * 22;
            if (isMouseOver(mouseX, mouseY, x, y, 20, 20)) {
                List<String> tip = InventoryHelper.getTooltip(pair.getKey().getResult(), ElecCore.proxy.getClientPlayer(), Minecraft.getMinecraft().gameSettings.advancedItemTooltips);

                if (!GuiScreen.isShiftKeyDown()) {
                    boolean hasInfo = tip.size() > 1;
                    while (tip.size() > 1) {
                        tip.remove(1);
                    }

                    StackList stacks = new StackList();
                    for (ItemStack stack : pair.getKey().getGrid()) {
                        if (!stack.isEmpty()) {
                            stacks.add(stack);
                        }
                    }

                    tip.add(TextFormatting.GRAY + I18n.format("gui." + Technicalities.MODID + ".tip.workbench.required") + ":");
                    stacks.forEachEntry((stack, amt) -> {
                        tip.add(TextFormatting.YELLOW + " " + amt + " " + stack.getDisplayName());
                        return true;
                    });

                    if (!pair.getValue().isEmpty()) {
                        tip.add(TextFormatting.GRAY + I18n.format("gui." + Technicalities.MODID + ".tip.workbench.missing") + ":");
                        pair.getValue().forEachEntry((stack, amt) -> {
                            tip.add(TextFormatting.RED + " " + amt + " " + stack.getDisplayName());
                            return true;
                        });
                    }

                    if (hasInfo) {
                        tip.add(TextFormatting.DARK_GRAY.toString() + TextFormatting.ITALIC + "<"
                                + I18n.format("gui." + Technicalities.MODID + ".tip.workbench.info") + ">");
                    }
                }

                return new ToolTip(tip);
            }
            index++;
        }
        return null;
    }
}

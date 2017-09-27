package com.technicalitiesmc.base.client.gui;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.base.init.TKBaseBlocks;
import com.technicalitiesmc.base.item.ItemRecipeBook.Recipe;
import com.technicalitiesmc.base.network.PacketGuiButton;
import com.technicalitiesmc.base.tile.TileWorkbench;
import com.technicalitiesmc.util.client.gui.GuiButton16;
import com.technicalitiesmc.util.stack.StackList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiUtils;

public class GuiWorkbench extends GuiContainer {

    private final TileWorkbench tile;

    public GuiWorkbench(Container container, TileWorkbench tile) {
        super(container);
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 209;
    }

    @Override
    public void initGui() {
        super.initGui();

        addButton(new GuiButton16(0, guiLeft + 152, guiTop + 17, 16, ""));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.pushMatrix();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translate(guiLeft, guiTop, 0);

        this.mc.getTextureManager().bindTexture(new ResourceLocation(Technicalities.MODID, "textures/gui/workbench.png"));
        this.drawTexturedModalRect(0, 0, 0, 0, this.xSize, this.ySize);

        if (!tile.getInventory().getStackInSlot(TileWorkbench.BOOK_START).isEmpty()) {
            this.mc.getTextureManager().bindTexture(new ResourceLocation(Technicalities.MODID, "textures/gui/workbench_book.png"));
            this.drawTexturedModalRect(-129, 0, 0, 0, 129, this.ySize);

            int index = 0;
            for (Pair<Recipe, StackList> pair : tile.getRecipes()) {
                int x = -110 + (index % 4) * 24, y = 15 + (index / 4) * 22;
                if (!pair.getValue().isEmpty()) {
                    drawRect(x, y, x + 20, y + 20, 0x7FFF4030);
                }
                if (isInArea(mouseX - guiLeft, mouseY - guiTop, x, y, 20, 20)) {
                    drawRect(x, y, x + 20, y + 20, 0x40302010);
                }
                index++;
            }

            RenderHelper.enableGUIStandardItemLighting();

            index = 0;
            for (Pair<Recipe, StackList> pair : tile.getRecipes()) {
                int x = -108 + (index % 4) * 24, y = 17 + (index / 4) * 22;
                itemRender.renderItemIntoGUI(pair.getKey().getResult(), x, y);
                if (isInArea(mouseX - guiLeft, mouseY - guiTop, x - 2, y - 2, 20, 20)) {
                    itemRender.renderItemOverlayIntoGUI(fontRenderer, pair.getKey().getResult(), x, y,
                            pair.getKey().getResult().getCount() + "");
                }
                index++;
            }
        }

        GlStateManager.popMatrix();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.mc.getTextureManager().bindTexture(new ResourceLocation(Technicalities.MODID, "textures/gui/workbench.png"));
        // Book icon
        this.drawTexturedModalRect(152, 17, xSize, 0, 16, 16);

        // Ink meter
        int inkAmt = tile.getInk();
        this.drawTexturedModalRect(152, 35 + 15 - inkAmt, xSize, 16 + 15 - inkAmt, 16, 16);

        String s = TKBaseBlocks.workbench.getLocalizedName();
        this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 6, 4210752);
        this.fontRenderer.drawString(Minecraft.getMinecraft().player.inventory.getDisplayName().getUnformattedText(), 8,
                this.ySize - 96 + 2, 4210752);

        int index = 0;
        for (Pair<Recipe, StackList> pair : tile.getRecipes()) {
            int x = -110 + (index % 4) * 24, y = 15 + (index / 4) * 22;
            if (isInArea(mouseX - guiLeft, mouseY - guiTop, x, y, 20, 20)) {
                List<String> tip = getItemToolTip(pair.getKey().getResult());

                if (!isShiftKeyDown()) {
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

                    tip.add(TextFormatting.GRAY + I18n.format("gui." + Technicalities.MODID + ":tip.workbench.required") + ":");
                    stacks.forEachEntry((stack, amt) -> {
                        tip.add(TextFormatting.YELLOW + " " + amt + " " + stack.getDisplayName());
                        return true;
                    });

                    if (!pair.getValue().isEmpty()) {
                        tip.add(TextFormatting.GRAY + I18n.format("gui." + Technicalities.MODID + ":tip.workbench.missing") + ":");
                        pair.getValue().forEachEntry((stack, amt) -> {
                            tip.add(TextFormatting.RED + " " + amt + " " + stack.getDisplayName());
                            return true;
                        });
                    }

                    if (hasInfo) {
                        tip.add(TextFormatting.DARK_GRAY.toString() + TextFormatting.ITALIC + "<"
                                + I18n.format("gui." + Technicalities.MODID + ":tip.workbench.info") + ">");
                    }
                }

                renderToolTip2(mouseX - guiLeft, mouseY - guiTop, pair.getKey().getResult(), tip);
            }
            index++;
        }
    }

    private void renderToolTip2(int x, int y, ItemStack stack, List<String> tip) {
        FontRenderer font = stack.getItem().getFontRenderer(stack);
        GuiUtils.preItemToolTip(stack);
        this.drawHoveringText(tip, x, y, (font == null ? fontRenderer : font));
        GuiUtils.postItemToolTip();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        int index = 0;
        for (Pair<Recipe, StackList> pair : tile.getRecipes()) {
            int x = -110 + (index % 4) * 24, y = 15 + (index / 4) * 22;
            if (pair.getValue().isEmpty() && isInArea(mouseX - guiLeft, mouseY - guiTop, x, y, 20, 20)) {
                if (mouseButton == 0) {
                    PacketGuiButton.send(tile.getPos(), index);
                } else if (mouseButton == 2 && isShiftKeyDown() && isCtrlKeyDown()) {
                    PacketGuiButton.send(tile.getPos(), index + 100);
                }
                return;
            }
            index++;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        if (button.id == 0) {
            PacketGuiButton.send(tile.getPos(), -1);
        }
    }

    private boolean isInArea(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

}

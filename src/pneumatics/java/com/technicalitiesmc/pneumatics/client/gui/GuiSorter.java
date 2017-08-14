package com.technicalitiesmc.pneumatics.client.gui;

import java.io.IOException;

import org.lwjgl.input.Mouse;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.lib.util.Tint;
import com.technicalitiesmc.pneumatics.tube.module.TMSorter;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;

public class GuiSorter extends GuiContainer {

    private final TMSorter sorter;
    private final boolean big;

    public GuiSorter(Container container, TMSorter sorter, boolean big) {
        super(container);
        this.xSize = 176;
        this.ySize = big ? 174 : 133;
        this.sorter = sorter;
        this.big = big;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.drawDefaultBackground();

        GlStateManager.pushMatrix();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translate((this.width - this.xSize) / 2, (this.height - this.ySize) / 2, 0);

        this.mc.getTextureManager()
                .bindTexture(new ResourceLocation(Technicalities.MODID, "textures/gui/tube_module/sorter" + (big ? 8 : 4) + ".png"));
        this.drawTexturedModalRect(0, 0, 0, 0, this.xSize, this.ySize);

        for (int j = 0; j < (big ? 2 : 1); j++) {
            for (int i = 0; i < 4; i++) {
                EnumDyeColor color = sorter.getColor(i + j * 4);
                if (color != null) {
                    int x = 57 + i * 18, y = 13 + j * 60;
                    drawRect(x, y, x + 8, y + 8, Tint.getColor(color).getRGB());
                }
            }
            EnumDyeColor color = sorter.getColor(-1 - j);
            if (color != null) {
                int x = 24, y = !big ? 24 : (35 + j * 16);
                drawRect(x, y, x + 8, y + 8, Tint.getColor(color).getRGB());
            }
        }
        EnumDyeColor color = sorter.getColor(-3);
        if (color != null) {
            int x = !big ? 127 : 131, y = !big ? 32 : 43;
            drawRect(x, y, x + 8, y + 8, Tint.getColor(color).getRGB());
        }

        GlStateManager.popMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        mouseX -= (this.width - this.xSize) / 2;
        mouseY -= (this.height - this.ySize) / 2;

        for (int j = 0; j < (big ? 2 : 1); j++) {
            for (int i = 0; i < 4; i++) {
                if (isInArea(mouseX, mouseY, 57 + i * 18, 13 + j * 60, 8, 8)) {
                    sorter.cycleColor(i + j * 4, mouseButton == 1);
                    return;
                }
            }
            if (isInArea(mouseX, mouseY, 24, !big ? 24 : (35 + j * 16), 8, 8)) {
                sorter.cycleColor(-1 - j, mouseButton == 1);
            }
        }
        if (isInArea(mouseX, mouseY, !big ? 127 : 131, !big ? 32 : 43, 8, 8)) {
            sorter.cycleColor(-3, mouseButton == 1);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int mouseX = (Mouse.getEventX() * width / mc.displayWidth) - (width - xSize) / 2;
            int mouseY = (height - Mouse.getEventY() * height / mc.displayHeight - 1) - (height - ySize) / 2;

            for (int j = 0; j < (big ? 2 : 1); j++) {
                for (int i = 0; i < 4; i++) {
                    if (isInArea(mouseX, mouseY, 57 + i * 18, 13 + j * 60, 8, 8)) {
                        sorter.cycleColor(i + j * 4, wheel < 0);
                        return;
                    }
                }
                if (isInArea(mouseX, mouseY, 24, !big ? 24 : (35 + j * 16), 8, 8)) {
                    sorter.cycleColor(-1 - j, wheel < 0);
                }
            }
            if (isInArea(mouseX, mouseY, !big ? 127 : 131, !big ? 32 : 43, 8, 8)) {
                sorter.cycleColor(-3, wheel < 0);
            }
        }
    }

    private boolean isInArea(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

}

package com.technicalitiesmc.pneumatics.client.gui;

import com.technicalitiesmc.pneumatics.TKPneumatics;
import com.technicalitiesmc.pneumatics.tube.module.TMFilter;
import com.technicalitiesmc.util.Tint;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class GuiFilter extends GuiContainer {

    private final TMFilter filter;
    private final boolean big;

    public GuiFilter(Container container, TMFilter filter, boolean big) {
        super(container);
        this.xSize = 176;
        this.ySize = big ? 174 : 133;
        this.filter = filter;
        this.big = big;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.drawDefaultBackground();

        GlStateManager.pushMatrix();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translate((this.width - this.xSize) / 2, (this.height - this.ySize) / 2, 0);

        this.mc.getTextureManager()
                .bindTexture(new ResourceLocation(TKPneumatics.MODID, "textures/gui/tube_module/filter" + (big ? 8 : 4) + ".png"));
        this.drawTexturedModalRect(0, 0, 0, 0, this.xSize, this.ySize);

        for (int j = 0; j < (big ? 2 : 1); j++) {
            for (int i = 0; i < 4; i++) {
                EnumDyeColor color = filter.getColor(i + j * 4);
                if (color != null) {
                    int x = 57 + i * 18, y = 32 + j * 22;
                    drawRect(x, y, x + 8, y + 8, Tint.getColor(color).getRGB());
                }
            }
            EnumDyeColor color = filter.getColor(-1 - j);
            if (color != null) {
                int x = 147, y = 24 + j * 38;
                drawRect(x, y, x + 8, y + 8, Tint.getColor(color).getRGB());
            }
        }

        GlStateManager.popMatrix();
        // System.out.println("< " + mouseX + " " + mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        mouseX -= (this.width - this.xSize) / 2;
        mouseY -= (this.height - this.ySize) / 2;

        for (int j = 0; j < (big ? 2 : 1); j++) {
            for (int i = 0; i < 4; i++) {
                if (isInArea(mouseX, mouseY, 57 + i * 18, 32 + j * 22, 8, 8)) {
                    filter.cycleColor(i + j * 4, mouseButton == 1);
                    return;
                }
            }
            if (isInArea(mouseX, mouseY, 147, 24 + j * 38, 8, 8)) {
                filter.cycleColor(-1 - j, mouseButton == 1);
            }
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
                    if (isInArea(mouseX, mouseY, 57 + i * 18, 32 + j * 22, 8, 8)) {
                        filter.cycleColor(i + j * 4, wheel < 0);
                        return;
                    }
                }
                if (isInArea(mouseX, mouseY, 147, 24 + j * 38, 8, 8)) {
                    filter.cycleColor(-1 - j, wheel < 0);
                }
            }
        }
    }

    private boolean isInArea(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

}

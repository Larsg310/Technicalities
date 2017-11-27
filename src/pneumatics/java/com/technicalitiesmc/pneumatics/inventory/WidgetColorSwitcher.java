package com.technicalitiesmc.pneumatics.inventory;

import com.technicalitiesmc.lib.Tint;
import com.technicalitiesmc.pneumatics.tube.module.IColorCycler;
import elec332.core.client.util.GuiDraw;
import elec332.core.inventory.widget.WidgetButton;
import elec332.core.inventory.window.Window;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.EnumDyeColor;

/**
 * Created by Elec332 on 27-11-2017.
 */
public class WidgetColorSwitcher extends WidgetButton {

    public WidgetColorSwitcher(int x, int y, IColorCycler colorCycler, int id) {
        super(x, y, 8, 8);
        this.sorter = colorCycler;
        this.id = id;
    }

    private IColorCycler sorter;
    private final int id;

    @Override
    public void draw(Window gui, int guiX, int guiY, int mouseX, int mouseY) {
        EnumDyeColor color = sorter.getColor(id);
        if (color != null) {
            GuiDraw.drawRect(x + guiX, y + guiY, x + width + guiX, y + height + guiY, Tint.getColor(color).getRGB());
            GlStateManager.color(1, 1, 1, 1);
        }
    }

    @Override
    public void onButtonClicked(int mouseButton) {
        super.onButtonClicked(mouseButton);
        sorter.cycleColor(id, mouseButton == 1);
    }

    @Override
    public boolean handleMouseWheel(int wheel, int translatedMouseX, int translatedMouseY) {
        if (isMouseOver(translatedMouseX, translatedMouseY)){
            sorter.cycleColor(id, wheel < 0);
            return true;
        }
        return super.handleMouseWheel(wheel, translatedMouseX, translatedMouseY);
    }

}

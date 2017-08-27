package com.technicalitiesmc.util.client.gui;

import com.technicalitiesmc.Technicalities;

import net.minecraft.util.ResourceLocation;

public class GuiButton16 extends GuiButtonResized {

    protected static final ResourceLocation TEXTURE = new ResourceLocation(Technicalities.MODID, "textures/gui/button16.png");

    public GuiButton16(int buttonId, int x, int y, int width, String buttonText) {
        super(buttonId, x, y, width, 16, buttonText, TEXTURE);
    }

}

package com.technicalitiesmc.util;

import java.awt.Color;
import java.util.EnumMap;
import java.util.Map;

import net.minecraft.item.EnumDyeColor;

public class Tint {

    private static final Map<EnumDyeColor, Color> COLORS = new EnumMap<>(EnumDyeColor.class);
    {
        COLORS.put(EnumDyeColor.WHITE, new Color(0xF9FFFE));
        COLORS.put(EnumDyeColor.ORANGE, new Color(0xF9801D));
        COLORS.put(EnumDyeColor.MAGENTA, new Color(0xC74EBD));
        COLORS.put(EnumDyeColor.LIGHT_BLUE, new Color(0x3AB3DA));
        COLORS.put(EnumDyeColor.YELLOW, new Color(0xFED83D));
        COLORS.put(EnumDyeColor.LIME, new Color(0x80C71F));
        COLORS.put(EnumDyeColor.PINK, new Color(0xF38BAA));
        COLORS.put(EnumDyeColor.GRAY, new Color(0x474F52));
        COLORS.put(EnumDyeColor.SILVER, new Color(0x9D9D97));
        COLORS.put(EnumDyeColor.CYAN, new Color(0x169C9C));
        COLORS.put(EnumDyeColor.PURPLE, new Color(0x8932B8));
        COLORS.put(EnumDyeColor.BLUE, new Color(0x3C44AA));
        COLORS.put(EnumDyeColor.BROWN, new Color(0x835432));
        COLORS.put(EnumDyeColor.GREEN, new Color(0x5E7C16));
        COLORS.put(EnumDyeColor.RED, new Color(0xB02E26));
        COLORS.put(EnumDyeColor.BLACK, new Color(0x1D1D21));
    }

    public static Color getColor(EnumDyeColor color) {
        return COLORS.get(color);
    }

}

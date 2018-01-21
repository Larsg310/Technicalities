package com.technicalitiesmc.electricity.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.EnumDyeColor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Elec332 on 19-1-2018.
 */
public class ColorHelper {

    private static final List<EnumDyeColor> colors;

    public static boolean hasWire(EnumDyeColor color, int colors) {
        return (colors & (1 << color.getMetadata())) != 0;
    }

    public static int addWire(EnumDyeColor color, int colors) {
        colors |= 1 << color.getMetadata();
        return colors;
    }

    public static int removeWire(EnumDyeColor color, int colors) {
        colors &= ~(1 << color.getMetadata());
        return colors;
    }

    public static List<EnumDyeColor> getColors(int colors){
        return ColorHelper.colors.stream().filter(enumDyeColor -> hasWire(enumDyeColor, colors)).collect(Collectors.toList());
    }

    static {
        colors = ImmutableList.copyOf(EnumDyeColor.values());
    }

}

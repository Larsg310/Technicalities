package com.technicalitiesmc.electricity.wires;

import com.google.common.collect.ImmutableList;
import com.technicalitiesmc.electricity.wires.WireColor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Elec332 on 19-1-2018.
 */
public class WireColorHelper {

    private static final List<WireColor> colorz;

    public static boolean hasWire(WireColor color, int colors) {
        return (colors & (1 << color.getMetadata())) != 0;
    }

    public static int addWire(WireColor color, int colors) {
        colors |= 1 << color.getMetadata();
        return colors;
    }

    public static int removeWire(WireColor color, int colors) {
        colors &= ~(1 << color.getMetadata());
        return colors;
    }

    public static List<WireColor> getColors(int colors){
        return colorz.stream().filter(enumDyeColor -> hasWire(enumDyeColor, colors)).collect(Collectors.toList());
    }

    static {
        colorz = ImmutableList.copyOf(WireColor.values());
    }

}

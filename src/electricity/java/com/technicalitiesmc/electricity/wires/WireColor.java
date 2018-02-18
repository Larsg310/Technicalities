package com.technicalitiesmc.electricity.wires;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.technicalitiesmc.api.electricity.EnumElectricityType;
import net.minecraft.item.EnumDyeColor;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.List;

/**
 * Created by Elec332 on 21-1-2018.
 */
public final class WireColor {

    @Nonnull
    public static WireColor getWireColor(@Nonnull EnumDyeColor color, @Nonnull EnumElectricityType type) {
        return data.get(Preconditions.checkNotNull(type)).get(Preconditions.checkNotNull(color));
    }

    private WireColor(EnumDyeColor color, EnumElectricityType type) {
        this.color = color;
        this.type = type;
        this.id = (type == EnumElectricityType.DC ? EnumDyeColor.values().length : 0) + color.ordinal();
    }

    private final EnumDyeColor color;
    private final EnumElectricityType type;
    private final int id;

    public EnumDyeColor getColor() {
        return color;
    }

    public EnumElectricityType getType() {
        return type;
    }

    public int getMetadata() {
        return id;
    }

    public static WireColor[] values() {
        return values.toArray(new WireColor[0]);
    }

    private static final EnumMap<EnumElectricityType, EnumMap<EnumDyeColor, WireColor>> data;
    private static final List<WireColor> values;

    static {
        data = new EnumMap<>(EnumElectricityType.class);
        List<WireColor> v = Lists.newArrayList();
        for (EnumElectricityType type : EnumElectricityType.values()) {
            EnumMap<EnumDyeColor, WireColor> m = new EnumMap<>(EnumDyeColor.class);
            data.put(type, m);
            for (EnumDyeColor color : EnumDyeColor.values()) {
                //WireColor c = EnumHelper.addEnum(WireColor.class, color.getName().toUpperCase()+"_"+type.toString(), new Class[]{EnumDyeColor.class, EnumElectricityType.class}, color, type);
                WireColor c = new WireColor(color, type);
                m.put(color, c);
                v.add(c);
            }
        }
        values = ImmutableList.copyOf(v);
    }

}

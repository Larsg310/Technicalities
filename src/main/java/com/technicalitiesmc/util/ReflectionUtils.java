package com.technicalitiesmc.util;

import com.google.common.base.Throwables;

import java.lang.reflect.Field;

public class ReflectionUtils {

    public static void setModifier(Field field, int modifier, boolean enabled) {
        try {
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            if (enabled) {
                modifiersField.setInt(field, field.getModifiers() | modifier);
            } else {
                modifiersField.setInt(field, field.getModifiers() & ~modifier);
            }
        } catch (Exception ex) {
            throw Throwables.propagate(ex);
        }
    }

}

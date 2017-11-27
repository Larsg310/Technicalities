package com.technicalitiesmc.lib.simple;

import com.google.common.base.Throwables;
import elec332.core.java.ReflectionHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

import java.lang.reflect.Field;

/**
 * Manages the registration of "simple registries".
 *
 * @see SimpleRegistry
 */
public enum SimpleRegistryManager {
    INSTANCE;

    /**
     * Creates a registry for all the classes marked with {@link SimpleRegistry @SimpleRegistry}.
     */
    @SuppressWarnings("unchecked")
    public <T extends IForgeRegistryEntry<T>> void init(ASMDataTable dataTable) {
        dataTable.getAll(SimpleRegistry.class.getName()).forEach(data -> {
            try {
                Class<?> clazz = Class.forName(data.getClassName());
                Field field = null;
                if (!data.getObjectName().equals(data.getClassName())) {
                    field = clazz.getDeclaredField(data.getObjectName());
                    if (!IForgeRegistry.class.isAssignableFrom(field.getType())) {
                        throw new IllegalStateException("Invalid field type. Must be a registry!");
                    }
                    SimpleRegistry ann = field.getAnnotation(SimpleRegistry.class);
                    if (ann.type() != IForgeRegistryEntry.class) {
                        clazz = ann.type();
                    } else {
                        clazz = Class.forName(field.getGenericType().getTypeName().split("<")[1].split(">")[0]);
                    }
                }
                IForgeRegistry<T> registry = new RegistryBuilder<T>()//
                        .setName(new ResourceLocation((String) data.getAnnotationInfo().get("value")))//
                        .setType((Class<T>) clazz)//
                        .setIDRange(0, Short.MAX_VALUE)//
                        .create();

                if (field != null) {
                    ReflectionHelper.makeFinalFieldModifiable(field);
                    field.set(null, registry);
                }
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        });
    }

}

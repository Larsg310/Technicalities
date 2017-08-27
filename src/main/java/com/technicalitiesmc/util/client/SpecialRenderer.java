package com.technicalitiesmc.util.client;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

/**
 * Marker for {@link TileEntity TileEntities} that automatically binds a {@link TileEntitySpecialRenderer} to them.
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface SpecialRenderer {

    /**
     * Gets the {@link TileEntitySpecialRenderer} class that will be bound to the {@link TileEntity}.
     */
    Class<? extends TileEntitySpecialRenderer<?>> value();

}

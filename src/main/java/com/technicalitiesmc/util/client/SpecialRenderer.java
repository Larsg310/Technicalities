package com.technicalitiesmc.util.client;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

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

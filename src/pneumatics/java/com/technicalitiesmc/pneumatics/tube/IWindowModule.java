package com.technicalitiesmc.pneumatics.tube;

import elec332.core.inventory.window.IWindowFactory;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;

/**
 * Created by Elec332 on 27-11-2017.
 */
public interface IWindowModule extends IWindowFactory {

    @Nonnull //Prevent warnings from IDE's
    BiConsumer<IWindowModule, EntityPlayer> openWindow = (a, b) -> {};

}

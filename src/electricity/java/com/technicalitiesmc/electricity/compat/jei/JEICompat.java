package com.technicalitiesmc.electricity.compat.jei;

import com.technicalitiesmc.electricity.init.ItemRegister;
import com.technicalitiesmc.electricity.item.ItemBundledWire;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * Created by Elec332 on 21-1-2018.
 */
@JEIPlugin
public class JEICompat implements IModPlugin {

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
        /*subtypeRegistry.registerSubtypeInterpreter(ItemRegister.bundledWire, new ISubtypeRegistry.ISubtypeInterpreter() {

            @Override
            @Nonnull
            public String apply(@Nonnull ItemStack itemStack) {
                return ItemBundledWire.getColorsFromStack(itemStack).toString();
            }

        });*/
    }

}

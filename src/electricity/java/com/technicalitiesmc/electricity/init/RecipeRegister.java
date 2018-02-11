package com.technicalitiesmc.electricity.init;

import com.technicalitiesmc.electricity.recipes.RecipeBundledWire;
import elec332.core.api.registration.IObjectRegister;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Created by Elec332 on 19-1-2018.
 */
public class RecipeRegister implements IObjectRegister<IRecipe> {

    @Override
    public void register(IForgeRegistry<IRecipe> registry) {
        registry.register(new RecipeBundledWire());
    }

    @Override
    public Class<IRecipe> getType() {
        return IRecipe.class;
    }

}

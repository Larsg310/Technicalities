package com.technicalitiesmc.pneumatics.item;

import java.util.List;

import com.technicalitiesmc.api.pneumatics.TubeModule;
import com.technicalitiesmc.lib.item.ItemBase;
import com.technicalitiesmc.pneumatics.tube.module.ModuleManager;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemTubeModule extends ItemBase {

    public ItemTubeModule() {
        setHasSubtypes(true);
        setCreativeTab(CreativeTabs.TRANSPORTATION);
    }

    @Override
    protected String getVariantName(ItemStack stack) {
        return ModuleManager.INSTANCE.get(stack.getMetadata()).getRegistryName().getResourcePath();
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!isInCreativeTab(tab)) {
            return;
        }
        List<TubeModule.Type<?>> types = ModuleManager.INSTANCE.getModuleTypes();
        for (int i = 0; i < types.size(); i++) {
            if (types.get(i) != null) {
                items.add(new ItemStack(this, 1, i));
            }
        }
    }

}

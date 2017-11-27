package com.technicalitiesmc.pneumatics.item;

import com.technicalitiesmc.api.pneumatics.TubeModule;
import com.technicalitiesmc.lib.item.ItemBase;
import com.technicalitiesmc.pneumatics.tube.module.ModuleManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

public class ItemTubeModule extends ItemBase {

    public ItemTubeModule() {
        setHasSubtypes(true);
        setCreativeTab(CreativeTabs.TRANSPORTATION);
    }

    @Override
    protected String getVariantName(ItemStack stack) {
        return ModuleManager.INSTANCE.getRegistryEntry(stack.getMetadata()).getRegistryName().getResourcePath();
    }

    @Override
    public void getSubItemsC(@Nonnull Item item, List<ItemStack> items, CreativeTabs tab) {
        if (!isInCreativeTab(tab)) {
            return;
        }
        Set<TubeModule.Type<?>> types = ModuleManager.INSTANCE.getModuleTypes();
        int i = 0;// TODO: Use registry IDs to avoid remapping issues
        for (TubeModule.Type<?> t : types) {
            if (t != null) {
                items.add(new ItemStack(this, 1, i));
            }
            i++;
        }
    }

}

package com.technicalitiesmc.util.item;

import com.technicalitiesmc.Technicalities;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;

@SuppressWarnings("deprecation")
public class ItemResource extends ItemBase {

    private final IResource[] resources;

    public ItemResource(IResource[] resources) {
        this.resources = resources;
        setHasSubtypes(true);
        setCreativeTab(CreativeTabs.MISC);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
        if (!isInCreativeTab(tab)) {
            return;
        }
        for (int i = 0; i < resources.length; i++) {
            IResource resource = resources[i];
            if (resource != null) {
                subItems.add(new ItemStack(this, 1, i));
            }
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        if (stack.getMetadata() >= resources.length) {
            return "item." + Technicalities.MODID + ":error";
        }

        IResource resource = resources[stack.getMetadata()];
        if (resource == null) {
            return "item." + Technicalities.MODID + ":error";
        }

        return getRegistryName() + "." + resource.getName();
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        String unlocalized = getUnlocalizedName(stack);
        String localized = I18n.translateToLocal(unlocalized);
        if (!localized.equals(unlocalized)) {
            return localized;
        }

        if (stack.getMetadata() >= resources.length) {
            return I18n.translateToLocal("item." + Technicalities.MODID + ":error");
        }

        IResource resource = resources[stack.getMetadata()];
        if (resource == null) {
            return I18n.translateToLocal("item." + Technicalities.MODID + ":error");
        }

        return I18n.translateToLocalFormatted("item." + getRegistryName() + ".name",
                I18n.translateToLocal("material." + Technicalities.MODID + ":" + resource.getName() + ".name"));
    }

    public static interface IResource {

        String getName();

    }

}

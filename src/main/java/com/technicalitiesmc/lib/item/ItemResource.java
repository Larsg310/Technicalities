package com.technicalitiesmc.lib.item;

import elec332.core.item.IEnumItem;
import elec332.core.item.ItemEnumBased;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public class ItemResource<E extends Enum<E> & IEnumItem> extends ItemEnumBased<E> implements ICanBreakOverride {

    public ItemResource(Class<E> clazz) {
        super(null, clazz);
        setCreativeTab(CreativeTabs.MISC);
    }

    private String unlName;

    protected String createUnlocalizedName(){
        return "item." + getRegistryName().toString().replace(":", ".").toLowerCase();
    }

    @Nonnull
    @Override
    public String getUnlocalizedName() {
        if (this.unlName == null){
            unlName = createUnlocalizedName();
        }
        return unlName;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) {
        return !canBreak(player.world, pos, stack);
    }

}

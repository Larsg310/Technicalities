package com.technicalitiesmc.api.pneumatics;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

public interface ITubeStack {

    public EnumDyeColor getColor();

    public ItemStack getStack();

    public ITubeStack withStack(ItemStack stack);

    public ITubeStack withColor(EnumDyeColor color);

}

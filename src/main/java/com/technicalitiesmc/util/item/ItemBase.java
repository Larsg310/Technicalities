package com.technicalitiesmc.util.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ItemBase extends Item {

    static {
        MinecraftForge.EVENT_BUS.register(ItemBase.class);
    }

    public ItemBase() {
        setCreativeTab(CreativeTabs.MISC);
    }

    @Override
    public String getUnlocalizedName() {
        return "item." + getRegistryName();
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        if (getHasSubtypes()) {
            return getUnlocalizedName() + "." + getVariantName(stack);
        }
        return getUnlocalizedName();
    }

    protected String getVariantName(ItemStack stack) {
        return Integer.toString(stack.getItemDamage());
    }

    protected boolean canBreak(World world, BlockPos pos, ItemStack stack) {
        return true;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) {
        return !canBreak(player.world, pos, stack);
    }

    @SubscribeEvent
    public static void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!stack.isEmpty() && stack.getItem() instanceof ItemBase
                && !((ItemBase) stack.getItem()).canBreak(event.getWorld(), event.getPos(), stack)) {
            event.setCanceled(true);
            event.getWorld().getBlockState(event.getPos()).getBlock().onBlockClicked(event.getWorld(), event.getPos(),
                    event.getEntityPlayer());
        }
    }

}

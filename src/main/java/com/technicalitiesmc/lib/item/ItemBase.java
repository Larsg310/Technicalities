package com.technicalitiesmc.lib.item;

import elec332.core.item.AbstractItem;
import elec332.core.world.WorldHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;

public class ItemBase extends AbstractItem implements ICanBreakOverride {

    public ItemBase() {
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

    @Nonnull
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

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) {
        return !canBreak(player.world, pos, stack);
    }

    @SubscribeEvent
    public static void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!stack.isEmpty() && stack.getItem() instanceof ICanBreakOverride && !((ICanBreakOverride) stack.getItem()).canBreak(event.getWorld(), event.getPos(), stack)) {
            event.setCanceled(true);
            WorldHelper.getBlockAt(event.getWorld(), event.getPos()).onBlockClicked(event.getWorld(), event.getPos(), event.getEntityPlayer());
        }
    }

    static {
        MinecraftForge.EVENT_BUS.register(ItemBase.class);
    }

}

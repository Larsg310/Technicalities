package com.technicalitiesmc.base.item;

import com.technicalitiesmc.base.manual.api.ManualAPI;
import com.technicalitiesmc.lib.item.ItemBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The manual!
 */
public final class ItemBookManual extends ItemBase {
    public static boolean tryOpenManual(final World world, final EntityPlayer player, @Nullable final String path) {
        if (path == null) {
            return false;
        }

        if (world.isRemote) {
            ManualAPI.openFor(player);
            ManualAPI.reset();
            ManualAPI.navigate(path);
        }

        return true;
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUseC(EntityPlayer player, EnumHand hand, World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return tryOpenManual(world, player, ManualAPI.pathFor(world, pos)) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClickC(EntityPlayer player, @Nonnull EnumHand hand, World world) {
        if (world.isRemote) {
            if (player.isSneaking()) {
                ManualAPI.reset();
            }
            ManualAPI.openFor(player);
        }
        return super.onItemRightClickC(player, hand, world);
    }

    @Override
    public boolean isEnchantable(final ItemStack stack) {
        return false;
    }

    @Override
    public int getItemEnchantability() {
        return 0;
    }
}

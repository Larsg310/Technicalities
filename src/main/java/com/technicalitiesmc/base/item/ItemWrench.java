package com.technicalitiesmc.base.item;

import com.technicalitiesmc.api.util.IBlockWrenchable;
import com.technicalitiesmc.lib.item.ItemBase;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ItemWrench extends ItemBase {
    public ItemWrench() {
        setMaxStackSize(1);
        setFull3D();
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUseC(EntityPlayer player, EnumHand hand, World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ) {
        Block block = world.getBlockState(pos).getBlock();

        boolean result = (block instanceof IBlockWrenchable)
            ? ((IBlockWrenchable) block).rotateBlock(world, pos, facing, player, hitX, hitY, hitZ)
            : block.rotateBlock(world, pos, facing);

        if (result) {
            playWrenchSound(world, pos);
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    public static void playWrenchSound(World world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.ITEM_ARMOR_EQUIP_IRON, SoundCategory.BLOCKS, 1.0f, 1.5f);
    }
}

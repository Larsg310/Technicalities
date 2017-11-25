package com.technicalitiesmc.util.item;

import elec332.core.item.AbstractItemBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber
public class ItemBlockBase extends AbstractItemBlock implements ICanBreakOverride {


    public ItemBlockBase(Block block) {
        super(block);
    }

    private String unlName;

    protected String createUnlocalizedName(){
        return this.block.getUnlocalizedName();
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
            return getUnlocalizedName() + "." + stack.getItemDamage();
        }
        return getUnlocalizedName();
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) {
        return !canBreak(player.world, pos, stack);
    }

}

package com.technicalitiesmc.lib.item;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by Elec332 on 25-11-2017.
 *
 * Horrible class name, I know.
 * Feel free to change if you have a better idea ;)
 */
public interface ICanBreakOverride {

	default public boolean canBreak(World world, BlockPos pos, ItemStack stack) {
		return true;
	}

}

package com.technicalitiesmc.base.manual.client.manual.provider;

import com.google.common.collect.Sets;
import com.technicalitiesmc.base.manual.api.manual.PathProvider;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class DefinitionPathProvider implements PathProvider {
    public static final List<String> VALID_MODS = Arrays.asList(
        "technicalities",
        "tkelectricity",
        "tkpneumatics",
        "tkmechanical"
    );

    public static Set<ItemStack> blacklist = Sets.newHashSet(ItemStack.EMPTY);

    public static boolean isBlacklisted(ItemStack stack) {
        return blacklist.stream().anyMatch(stack::isItemEqual);
    }

    @Nullable
    @Override
    public String pathFor(ItemStack stack) {
        String modid = stack.getItem().getRegistryName().getResourceDomain();
        if (isBlacklisted(stack) || !VALID_MODS.contains(modid))
            return null;
        String path = stack.getUnlocalizedName().replace(String.format("item.%s.", modid), "");
        return "%LANGUAGE%/items/" + path + ".md";
    }

    @Nullable
    @Override
    public String pathFor(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        int meta = block.damageDropped(state);
        ItemStack stack = new ItemStack(block, 1, meta);
        String modid = stack.getItem().getRegistryName().getResourceDomain();
        if (isBlacklisted(stack) || !VALID_MODS.contains(modid))
            return null;
        String path = stack.getUnlocalizedName().replace(String.format("tile.%s.", modid), "");
        return "%LANGUAGE%/blocks/" + path + ".md";
    }
}

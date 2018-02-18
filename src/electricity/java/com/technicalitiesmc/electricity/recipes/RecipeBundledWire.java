package com.technicalitiesmc.electricity.recipes;

import com.google.common.collect.Sets;
import com.technicalitiesmc.api.electricity.EnumElectricityType;
import com.technicalitiesmc.electricity.init.ItemRegister;
import com.technicalitiesmc.electricity.item.ItemBundledWire;
import com.technicalitiesmc.electricity.wires.ground.WirePart;
import com.technicalitiesmc.electricity.util.TKEResourceLocation;
import com.technicalitiesmc.electricity.wires.WireColor;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * Created by Elec332 on 11-2-2018.
 */
public class RecipeBundledWire implements IRecipe {

    @Override
    public boolean matches(@Nonnull InventoryCrafting inv, @Nonnull World worldIn) {
        Set<WireColor> colors = Sets.newHashSet();
        int items = 0;
        int size = 0;
        for (ItemStack stack : inv.stackList) {
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() == ItemRegister.bundledWire) {
                Pair<Integer, List<WireColor>> data = ItemBundledWire.getColorsFromStack(stack);
                if (size == 0) {
                    size = data.getLeft();
                } else if (size != data.getLeft()) {
                    return false;
                }
                List<WireColor> c = data.getRight();
                for (WireColor color : c) {
                    if (colors.contains(color)) {
                        return false;
                    }
                }
                colors.addAll(c);
                items++;
                continue;
            }
            return false;
        }
        return colors.size() <= WirePart.getMaxWires(size) && items > 1;
    }

    @Override
    @Nonnull
    public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
        Set<WireColor> colors = Sets.newHashSet();
        int size = 1;
        for (ItemStack stack : inv.stackList) {
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() == ItemRegister.bundledWire) {
                Pair<Integer, List<WireColor>> data = ItemBundledWire.getColorsFromStack(stack);
                size = data.getLeft();
                colors.addAll(data.getRight());
            }
        }
        return ItemBundledWire.withCables(colors, size);
    }

    @Override
    public boolean canFit(int width, int height) {
        return true;
    }

    @Override
    @Nonnull
    public ItemStack getRecipeOutput() {
        return ItemBundledWire.withCables(1, WireColor.getWireColor(EnumDyeColor.WHITE, EnumElectricityType.AC));
    }

    @Override
    public IRecipe setRegistryName(ResourceLocation name) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return new TKEResourceLocation("bundled_wire_recipe");
    }

    @Override
    public Class<IRecipe> getRegistryType() {
        return IRecipe.class;
    }

}

package com.technicalitiesmc.base.item;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.technicalitiesmc.util.item.ItemBase;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;

public class ItemRecipeBook extends ItemBase {

    public static List<Recipe> getRecipes(ItemStack book) {
        List<Recipe> recipes = new LinkedList<>();
        if (!book.isEmpty() && book.hasTagCompound()) {
            NBTTagList list = book.getTagCompound().getTagList("recipes", NBT.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                Recipe recipe = new Recipe();
                recipe.deserializeNBT(list.getCompoundTagAt(i));
                recipes.add(recipe);
            }
        }
        return recipes;
    }

    public static boolean addRecipe(ItemStack book, Recipe recipe) {
        NBTTagCompound tag = book.getTagCompound();
        if (tag == null) {
            book.setTagCompound(tag = new NBTTagCompound());
        }
        NBTTagList recipes = tag.getTagList("recipes", NBT.TAG_COMPOUND);
        if (recipes.tagCount() < 32) {
            NBTTagCompound nbt = recipe.serializeNBT();
            for (int i = 0; i < recipes.tagCount(); i++) {
                if (recipes.getCompoundTagAt(i).equals(nbt)) {
                    return false;
                }
            }
            recipes.appendTag(nbt);
            tag.setTag("recipes", recipes);
            return true;
        }
        return false;
    }

    public static boolean removeRecipe(ItemStack book, int recipe) {
        NBTTagCompound tag = book.getTagCompound();
        if (tag == null) {
            book.setTagCompound(tag = new NBTTagCompound());
        }
        NBTTagList recipes = tag.getTagList("recipes", NBT.TAG_COMPOUND);
        if (recipes.tagCount() > recipe) {
            recipes.removeTag(recipe);
            tag.setTag("recipes", recipes);
            return true;
        }
        return false;
    }

    public static class Recipe implements INBTSerializable<NBTTagCompound> {

        private final ItemStack[] grid = new ItemStack[9];
        private ItemStack result = ItemStack.EMPTY;
        private NonNullList<ItemStack> remainder = NonNullList.create();

        public Recipe() {
            Arrays.fill(grid, ItemStack.EMPTY);
        }

        public ItemStack[] getGrid() {
            return grid;
        }

        public ItemStack getResult() {
            return result;
        }

        public NonNullList<ItemStack> getRemainder() {
            return remainder;
        }

        public static Recipe fromGrid(InventoryCrafting craftingGrid, World world) {
            IRecipe mcRecipe = CraftingManager.findMatchingRecipe(craftingGrid, world);
            if (mcRecipe == null) {
                return null;
            }

            Recipe recipe = new Recipe();
            for (int i = 0; i < 9; i++) {
                recipe.grid[i] = craftingGrid.getStackInSlot(i).copy().splitStack(1);
            }
            recipe.result = mcRecipe.getCraftingResult(craftingGrid);
            for (ItemStack s : mcRecipe.getRemainingItems(craftingGrid)) {
                if (!s.isEmpty()) {
                    recipe.remainder.add(s);
                }
            }
            return recipe;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound tag = new NBTTagCompound();

            NBTTagList grid = new NBTTagList();
            for (int i = 0; i < 9; i++) {
                if (this.grid[i] != null) {
                    grid.appendTag(this.grid[i].serializeNBT());
                } else {
                    grid.appendTag(new NBTTagCompound());
                }
            }
            tag.setTag("grid", grid);

            tag.setTag("result", result.serializeNBT());

            NBTTagList remainder = new NBTTagList();
            for (ItemStack stack : this.remainder) {
                remainder.appendTag(stack.serializeNBT());
            }
            tag.setTag("remainder", remainder);

            return tag;
        }

        @Override
        public void deserializeNBT(NBTTagCompound tag) {
            NBTTagList grid = tag.getTagList("grid", NBT.TAG_COMPOUND);
            for (int i = 0; i < 9; i++) {
                NBTTagCompound t = grid.getCompoundTagAt(i);
                if (!t.hasNoTags()) {
                    this.grid[i] = new ItemStack(t);
                } else {
                    this.grid[i] = ItemStack.EMPTY;
                }
            }

            result = new ItemStack(tag.getCompoundTag("result"));

            NBTTagList remainder = tag.getTagList("remainder", NBT.TAG_COMPOUND);
            for (int i = 0; i < remainder.tagCount(); i++) {
                this.remainder.add(new ItemStack(remainder.getCompoundTagAt(i)));
            }
        }

    }

}

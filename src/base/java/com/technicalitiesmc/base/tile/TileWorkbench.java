package com.technicalitiesmc.base.tile;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.technicalitiesmc.base.init.TKBaseItems;
import com.technicalitiesmc.base.item.ItemRecipeBook;
import com.technicalitiesmc.base.item.ItemRecipeBook.Recipe;
import com.technicalitiesmc.lib.block.TileBase;
import com.technicalitiesmc.lib.inventory.SimpleItemHandler;
import com.technicalitiesmc.lib.stack.StackList;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

public class TileWorkbench extends TileBase {

    public static final int INV_START = 0, INV_SIZE = 2 * 9;
    public static final int BOOK_START = INV_START + INV_SIZE, BOOK_SIZE = 1 + 2;
    public static final int GRID_START = BOOK_START + BOOK_SIZE, GRID_SIZE = 3 * 3 + 1;
    public static final int INVENTORY_SIZE = GRID_START + GRID_SIZE;

    private final SimpleItemHandler inventory = new SimpleItemHandler(INVENTORY_SIZE, this::onInventoryUpdate);
    private final ExposedInventory exposedInventory = new ExposedInventory();

    private final GridInventory craftingGrid = new GridInventory();
    private final InventoryCrafting inventoryCrafting = new InventoryCrafting(null, 3, 3);

    private final List<Pair<Recipe, StackList>> recipes = new LinkedList<>();

    public TileWorkbench() {
        inventory.withFilter(TKBaseItems.recipe_book, BOOK_START);
        inventory.withFilter(Items.PAPER, BOOK_START + 1);
        inventory.withFilter(new ItemStack(Items.DYE, 1, 0), BOOK_START + 2);
    }

    public SimpleItemHandler getInventory() {
        return inventory;
    }

    public ExposedInventory getExposedInventory() {
        return exposedInventory;
    }

    public GridInventory getCraftingGrid() {
        return craftingGrid;
    }

    public InventoryCrafting getGrid() {
        return inventoryCrafting;
    }

    public List<Pair<Recipe, StackList>> getRecipes() {
        return recipes;
    }

    private void onInventoryUpdate(int slot) {
        if (slot >= INV_START && slot < INV_START + INV_SIZE) {
            updateAvailableRecipes();
        } else if (slot == BOOK_START) {
            updateRecipeBook();
        } else if (slot >= GRID_START && slot < GRID_START + GRID_SIZE) {
            refreshRecipe(slot);
            updateAvailableRecipes();
        }

        markDirty();
    }

    private void updateAvailableRecipes() {
        recipes.replaceAll(pair -> Pair.of(pair.getKey(), computeRequirements(pair.getKey())));
    }

    private void updateRecipeBook() {
        recipes.clear();
        for (Recipe recipe : ItemRecipeBook.getRecipes(inventory.getStackInSlot(BOOK_START))) {
            recipes.add(Pair.of(recipe, computeRequirements(recipe)));
        }
    }

    private StackList computeRequirements(Recipe recipe) {
        StackList required = new StackList();
        for (ItemStack stack : recipe.getGrid()) {
            if (!stack.isEmpty()) {
                stack = stack.copy();
                stack.setCount(1);
                required.add(stack);
            }
        }

        for (ItemStack stack : exposedInventory) {
            required.remove(stack, stack.getCount());
        }
        for (int i = 0; i < 9; i++) {
            ItemStack stack = craftingGrid.getStackInSlot(i);
            required.remove(stack, stack.getCount());
        }
        return required;
    }

    private void refreshRecipe(int slot) {
        if (slot < GRID_START + GRID_SIZE - 1) {
            inventoryCrafting.stackList.set(slot - GRID_START, inventory.getStackInSlot(slot));
        } else if (!craftingGrid.getStackInSlot(9).isEmpty()) {
            markDirty();
            return;
        }
        refreshRecipe();
    }

    private void refreshRecipe() {
        IRecipe recipe = CraftingManager.findMatchingRecipe(inventoryCrafting, getWorld());
        if (recipe != null) {
            inventory.setStackInSlot(GRID_START + 9, recipe.getCraftingResult(inventoryCrafting));
        } else {
            inventory.getContents().set(GRID_START + 9, ItemStack.EMPTY);
        }
    }

    public void pullStack(ItemStack stack) {
        for (ItemStack s : exposedInventory) {
            if (ItemHandlerHelper.canItemStacksStack(s, stack)) {
                s.shrink(1);
                return;
            }
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) exposedInventory;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        tag.setTag("inventory", inventory.serializeNBT());
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        inventory.deserializeNBT(tag.getCompoundTag("inventory"));

        updateRecipeBook();

        for (int i = 0; i < 9; i++) {
            inventoryCrafting.stackList.set(i, inventory.getStackInSlot(i));
        }
        refreshRecipe();
    }

    public class ExposedInventory implements IItemHandler, Iterable<ItemStack> {

        private ExposedInventory() {
        }

        @Override
        public int getSlots() {
            return INV_SIZE;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot < INV_SIZE ? inventory.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot < INV_SIZE) {
                return inventory.insertItem(slot, stack, simulate);
            }
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot < INV_SIZE) {
                return inventory.extractItem(slot, amount, simulate);
            }
            return null;
        }

        @Override
        public int getSlotLimit(int slot) {
            if (slot < INV_SIZE) {
                return inventory.getSlotLimit(slot);
            }
            return 0;
        }

        @Override
        public Iterator<ItemStack> iterator() {
            return new Iterator<ItemStack>() {

                private int i = 0;

                @Override
                public boolean hasNext() {
                    return i < getSlots();
                }

                @Override
                public ItemStack next() {
                    return getStackInSlot(i++);
                }

            };
        }

    }

    public class GridInventory implements IItemHandlerModifiable {

        @Override
        public int getSlots() {
            return GRID_SIZE;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(GRID_START + slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return inventory.insertItem(GRID_START + slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot == 9 && !simulate) {
                IRecipe recipe = CraftingManager.findMatchingRecipe(inventoryCrafting, getWorld());
                if (recipe != null) {
                    inventory.setStackInSlot(GRID_START + 9, recipe.getCraftingResult(inventoryCrafting));
                    // TODO: Add leftovers
                    for (int i = 0; i < 9; i++) {
                        extractCraftingComponent(i);
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }
            return inventory.extractItem(GRID_START + slot, amount, simulate);
        }

        private void extractCraftingComponent(int slot) {
            ItemStack stack = getStackInSlot(slot);
            if (stack.isEmpty()) {
                return;
            }

            if (stack.getCount() > 1) {
                stack.shrink(1);
                return;
            }

            for (ItemStack s : exposedInventory) {
                if (ItemHandlerHelper.canItemStacksStack(s, stack)) {
                    s.shrink(1);
                    return;
                }
            }

            stack.shrink(1);
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getSlotLimit(GRID_START + slot);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            inventory.setStackInSlot(GRID_START + slot, stack);
        }

    }

}

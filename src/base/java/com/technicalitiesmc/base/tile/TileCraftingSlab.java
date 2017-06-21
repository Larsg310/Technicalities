package com.technicalitiesmc.base.tile;

import java.io.IOException;

import com.technicalitiesmc.base.client.render.TESRCraftingSlab;
import com.technicalitiesmc.lib.block.TileBase;
import com.technicalitiesmc.lib.client.SpecialRenderer;
import com.technicalitiesmc.lib.sync.NBTSaveable;
import com.technicalitiesmc.lib.sync.SyncedField;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

// FIXME: Grid offsets for recipes smaller than 3x3
// A bucket aligned to the bottom will have the crafting items placed at the top
@SpecialRenderer(TESRCraftingSlab.class)
public class TileCraftingSlab extends TileBase {

    private static final int DELAY = 5;

    private final Container dummyContainer = new Container() {

        @Override
        public boolean canInteractWith(EntityPlayer player) {
            return true;
        }

        @Override
        public void onCraftMatrixChanged(IInventory inventoryIn) {
            markDirty();
        }

    };
    private final InventoryCrafting grid = new InventoryCrafting(dummyContainer, 3, 3);
    private final NonNullList<ItemStack> outputStacks = NonNullList.create();
    @NBTSaveable(INBTSerializable.class)
    @SyncedField(INBTSerializable.class)
    private final ItemStackHandler recipeItems = new ItemStackHandler(9);

    @NBTSaveable
    @SyncedField
    private IRecipe recipe = null;
    @NBTSaveable
    @SyncedField
    private boolean locked = false;

    public ItemStack getGridItem(int x, int y) {
        return grid.getStackInRowAndColumn(x, y);
    }

    public ItemStack getStack(int x, int y) {
        ItemStack stack = grid.getStackInRowAndColumn(x, y);
        if (stack.isEmpty()) {
            stack = recipeItems.getStackInSlot(x + 3 * y);
        }
        return stack;
    }

    public boolean isStackPresent(int x, int y) {
        return !grid.getStackInRowAndColumn(x, y).isEmpty();
    }

    public void setStack(int x, int y, ItemStack stack) {
        grid.setInventorySlotContents(x + 3 * y, stack);
        updateRecipe();
        markDirty();
    }

    private void updateRecipe() {
        recipe = CraftingManager.findMatchingRecipe(grid, getWorld());
    }

    public ItemStack getResult() {
        if (!outputStacks.isEmpty()) {
            return outputStacks.get(0);
        }
        return recipe == null ? ItemStack.EMPTY : recipe.getCraftingResult(grid);
    }

    public boolean hasResult() {
        return !outputStacks.isEmpty();
    }

    public ItemStack retrieveOutput() {
        if (outputStacks.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = outputStacks.get(0);
        outputStacks.remove(0);
        markDirty();

        if (outputStacks.isEmpty()) {
            getWorld().scheduleUpdate(getPos(), getBlockType(), DELAY);
        }

        return stack;
    }

    public void lock() {
        locked = true;
        for (int i = 0; i < 9; i++) {
            recipeItems.setStackInSlot(i, grid.getStackInSlot(i));
        }
        grid.clear();
        getWorld().scheduleUpdate(getPos(), getBlockType(), DELAY);
        markDirty();
    }

    public boolean isLocked() {
        return locked;
    }

    public void update() {
        if (recipe == null) {
            getWorld().scheduleUpdate(getPos(), getBlockType(), DELAY);
            return;
        }
        if (!recipe.matches(grid, getWorld())) {
            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            for (int i = 0; i < Math.min(ingredients.size(), 9); i++) {
                Ingredient ingredient = ingredients.get(i);
                if (ingredient != Ingredient.EMPTY && !ingredient.test(grid.getStackInSlot(i))) {
                    TileEntity tileBelow = getWorld().getTileEntity(getPos().down());
                    if (tileBelow != null && tileBelow.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP)) {
                        IItemHandler inv = tileBelow.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
                        for (int s = 0; s < inv.getSlots(); s++) {
                            ItemStack stack = inv.extractItem(s, 1, true);
                            if (!stack.isEmpty() && ingredient.test(stack)) {
                                grid.setInventorySlotContents(i, inv.extractItem(s, 1, false));
                                markDirty();
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        } else if (outputStacks.isEmpty()) {
            outputStacks.add(recipe.getCraftingResult(grid));
            recipe.getRemainingItems(grid).forEach(s -> {
                if (!s.isEmpty()) {
                    outputStacks.add(s);
                }
            });
            grid.clear();
            markDirty();
        } else {
            return; // Don't tick again
        }
        getWorld().scheduleUpdate(getPos(), getBlockType(), DELAY);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);

        NBTTagList grid = new NBTTagList();
        for (int i = 0; i < 9; i++) {
            grid.appendTag(this.grid.getStackInSlot(i).writeToNBT(new NBTTagCompound()));
        }
        tag.setTag("grid", grid);

        NBTTagList out = new NBTTagList();
        for (ItemStack stack : outputStacks) {
            out.appendTag(stack.writeToNBT(new NBTTagCompound()));
        }
        tag.setTag("out", out);

        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        NBTTagList grid = tag.getTagList("grid", NBT.TAG_COMPOUND);
        for (int i = 0; i < 9; i++) {
            this.grid.setInventorySlotContents(i, new ItemStack(grid.getCompoundTagAt(i)));
        }

        outputStacks.clear();
        NBTTagList out = tag.getTagList("out", NBT.TAG_COMPOUND);
        for (int i = 0; i < out.tagCount(); i++) {
            outputStacks.add(new ItemStack(out.getCompoundTagAt(i)));
        }
    }

    @Override
    public NBTTagCompound writeDescription(NBTTagCompound tag) {
        tag = super.writeDescription(tag);

        NBTTagList grid = new NBTTagList();
        for (int i = 0; i < 9; i++) {
            grid.appendTag(this.grid.getStackInSlot(i).writeToNBT(new NBTTagCompound()));
        }
        tag.setTag("grid", grid);

        if (!outputStacks.isEmpty()) {
            tag.setTag("out", outputStacks.get(0).writeToNBT(new NBTTagCompound()));
        }

        return tag;
    }

    @Override
    public void readDescription(NBTTagCompound tag) {
        super.readDescription(tag);

        NBTTagList grid = tag.getTagList("grid", NBT.TAG_COMPOUND);
        for (int i = 0; i < 9; i++) {
            this.grid.setInventorySlotContents(i, new ItemStack(grid.getCompoundTagAt(i)));
        }

        outputStacks.clear();
        if (tag.hasKey("out")) {
            outputStacks.add(new ItemStack(tag.getCompoundTag("out")));
        }
    }

    @Override
    public void writeUpdateExtra(PacketBuffer buf) {
        super.writeUpdateExtra(buf);

        for (int i = 0; i < 9; i++) {
            buf.writeItemStack(grid.getStackInSlot(i));
        }

        if (!outputStacks.isEmpty()) {
            buf.writeBoolean(true);
            buf.writeItemStack(outputStacks.get(0));
        } else {
            buf.writeBoolean(false);
        }
    }

    @Override
    public void readUpdateExtra(PacketBuffer buf) {
        super.readUpdateExtra(buf);

        for (int i = 0; i < 9; i++) {
            try {
                grid.setInventorySlotContents(i, buf.readItemStack());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        outputStacks.clear();
        if (buf.readBoolean()) {
            try {
                outputStacks.add(buf.readItemStack());
            } catch (IOException e) {
            }
        }
    }

}

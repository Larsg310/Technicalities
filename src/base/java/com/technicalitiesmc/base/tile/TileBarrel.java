package com.technicalitiesmc.base.tile;

import com.technicalitiesmc.lib.block.TileBase;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileBarrel extends TileBase implements IItemHandler {

    private ItemStack type = ItemStack.EMPTY;
    private int amount, maxStacks;

    public TileBarrel(int maxStacks) {
        this.maxStacks = maxStacks;
    }

    public ItemStack getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public int getMaxAmount() {
        return (type.isEmpty() ? 64 : type.getMaxStackSize()) * maxStacks;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        ItemStack stack = type.copy();
        stack.setCount(Math.max(amount, stack.getMaxStackSize()));
        return stack;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (type.isEmpty()) {
            type = stack.copy();
            type.setCount(1);
        } else if (!ItemStack.areItemsEqual(stack, type) || !ItemStack.areItemStackTagsEqual(stack, type)) {
            return stack;
        } else if (this.amount == type.getMaxStackSize() * maxStacks) {
            return stack;
        }
        int inserted = Math.min(stack.getCount(), type.getMaxStackSize() * maxStacks - this.amount);
        ItemStack leftover = stack.copy();
        leftover.splitStack(inserted);
        if (!simulate) {
            this.amount += inserted;
        }
        return leftover;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        int amt = Math.min(amount, this.amount);
        if (amt == 0) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = type.copy();
        if (!simulate) {
            this.amount -= amt;
            if (this.amount == 0) {
                type = ItemStack.EMPTY;
            }
        }
        stack.setCount(amt);
        return stack;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
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
            return (T) this;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        tag.setTag("type", type.writeToNBT(new NBTTagCompound()));
        tag.setInteger("amount", amount);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        type = new ItemStack(tag.getCompoundTag("type"));
        amount = tag.getInteger("amount");
    }

}

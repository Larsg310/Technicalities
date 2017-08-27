package com.technicalitiesmc.base.tile;

import com.technicalitiesmc.util.block.TileBase;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileBarrel extends TileBase implements IItemHandler, IFluidHandler {

    private ItemStack type = ItemStack.EMPTY;
    private int amount, maxStacks;
    private final FluidTank tank;

    private TileBarrel(int maxStacks) {
        this.maxStacks = maxStacks;
        this.tank = new FluidTank(maxStacks * Fluid.BUCKET_VOLUME);
    }

    public TileBarrel() {
        this(32);
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
        if (tank.getFluidAmount() > 0) {
            return stack;
        }
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
    public IFluidTankProperties[] getTankProperties() {
        return tank.getTankProperties();
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (amount > 0) {
            return 0;
        }
        return tank.fill(resource, doFill);
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        return tank.drain(resource, doDrain);
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return tank.drain(maxDrain, doDrain);
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
        tag.setTag("tank", tank.writeToNBT(new NBTTagCompound()));
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        type = new ItemStack(tag.getCompoundTag("type"));
        amount = tag.getInteger("amount");
        tank.readFromNBT(tag.getCompoundTag("tank"));
    }

}

package com.technicalitiesmc.lib.inventory;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

public class SimpleItemHandler extends ItemStackHandler {

    private final IntConsumer updateCallback;
    @SuppressWarnings("rawtypes")
    private final Predicate[] filters;
    private final BooleanSupplier[] noExtraction;

    public SimpleItemHandler(int slots, IntConsumer updateCallback) {
        super(slots);
        this.updateCallback = updateCallback;
        this.filters = new Predicate[slots];
        this.noExtraction = new BooleanSupplier[slots];
    }

    public SimpleItemHandler(int slots, Runnable updateCallback) {
        this(slots, i -> updateCallback.run());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected int getStackLimit(int slot, ItemStack stack) {
        Predicate<ItemStack> filter = filters[slot];
        if (filter != null && !filter.test(stack)) {
            return 0;
        }
        return super.getStackLimit(slot, stack);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (noExtraction[slot] != null && noExtraction[slot].getAsBoolean()) {
            return ItemStack.EMPTY;
        }
        return super.extractItem(slot, amount, simulate);
    }

    @Override
    protected void onContentsChanged(int slot) {
        updateCallback.accept(slot);
    }

    public boolean isEmpty() {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public NonNullList<ItemStack> getContents() {
        return stacks;
    }

    public SimpleItemHandler withFilter(ItemStack filter, int... slots) {
        return withFilter(s -> ItemHandlerHelper.canItemStacksStack(s, filter), slots);
    }

    public SimpleItemHandler withFilter(Item filter, int... slots) {
        return withFilter(s -> s.getItem() == filter, slots);
    }

    public SimpleItemHandler withFilter(Predicate<ItemStack> filter, int... slots) {
        for (int slot : slots) {
            filters[slot] = filter;
        }
        return this;
    }

    public SimpleItemHandler disableExtraction(int... slots) {
        return disableExtraction(() -> true, slots);
    }

    public SimpleItemHandler disableExtraction(BooleanSupplier test, int... slots) {
        for (int slot : slots) {
            noExtraction[slot] = test;
        }
        return this;
    }

}

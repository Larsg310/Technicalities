package com.technicalitiesmc.lib.stack;

import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;
import net.minecraft.item.ItemStack;

public class StackList extends TObjectIntCustomHashMap<ItemStack> {

    public StackList() {
        super(StackHashingStrategy.INSTANCE);
    }

    public boolean add(ItemStack stack) {
        if (!stack.isEmpty()) {
            put(stack, get(stack) + stack.getCount());
            return true;
        }
        return false;
    }

    public boolean remove(ItemStack stack, int amount) {
        if (stack.isEmpty()) {
            return false;
        }

        int currentAmount = get(stack);
        if (currentAmount == 0) {
            return false;
        }

        int newAmount = currentAmount - amount;
        if (newAmount > 0) {
            put(stack, newAmount);
        } else {
            remove(stack);
        }

        return true;
    }

}

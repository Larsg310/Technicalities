package com.technicalitiesmc.lib.stack;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;
import java.util.function.Predicate;

public class AbstractStack implements Predicate<ItemStack> {

    private static final AbstractStack EMPTY = new AbstractStack();

    private static final Map<Integer, List<ItemStack>> OREDICT_ENTRIES = new HashMap<>();

    private List<ItemStack> stacks;
    private List<ItemStack> stacksView;
    private Set<Integer> oreDictEntries;

    private AbstractStack() {
    }

    private AbstractStack(AbstractStack item) {
        this.stacks = item.stacks == null ? null : Lists.newArrayList(item.stacks);
        this.stacksView = this.stacks == null ? null : Collections.unmodifiableList(this.stacks);
        this.oreDictEntries = item.oreDictEntries == null ? null : new HashSet<>(item.oreDictEntries);
    }

    public List<ItemStack> getItems() {
        if (this.stacks == null && this.oreDictEntries == null) {
            return Collections.emptyList();
        }
        if (this.stacks != null && this.oreDictEntries == null) {
            return this.stacksView;
        }
        if (this.stacks == null && this.oreDictEntries.size() == 1) {
            return OREDICT_ENTRIES.get(this.oreDictEntries.iterator().next());
        }
        List<ItemStack> stacks = Lists.newArrayList();
        if (this.stacks != null) {
            this.stacks.addAll(stacks);
        }
        if (this.oreDictEntries != null) {
            for (int entry : this.oreDictEntries) {
                stacks.addAll(OREDICT_ENTRIES.get(entry));
            }
        }
        return Collections.unmodifiableList(stacks);
    }

    @Override
    public boolean test(ItemStack stack) {
        if (this.stacks != null) {
            for (ItemStack is : this.stacks) {
                if (ItemStack.areItemsEqual(stack, is) && ItemStack.areItemStackTagsEqual(stack, is)) {
                    return true;
                }
            }
        }
        if (this.oreDictEntries != null) {
            for (int id : OreDictionary.getOreIDs(stack)) {
                if (oreDictEntries.contains(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Creates a new {@link QuantumStack} that returns this {@link AbstractStack} with the specified chance.
     */
    public QuantumStack withChance(float chance) {
        Iterator<ItemStack> it = getItems().iterator();
        if (!it.hasNext()) {
            throw new IllegalStateException("Abstract stack does not have any sample items.");
        }
        return QuantumStack.of(it.next(), chance);
    }

    public AbstractStack with(ItemStack... stacks) {
        AbstractStack item = new AbstractStack(this);
        if (item.stacks == null) {
            item.stacks = Lists.newArrayList();
            item.stacksView = Collections.unmodifiableList(item.stacks);
        }
        for (ItemStack stack : stacks) {
            ItemStack theStack = stack.copy();
            theStack.setCount(1);
            item.stacks.add(theStack);
        }
        return item;
    }

    public AbstractStack with(Item... items) {
        AbstractStack item = new AbstractStack(this);
        if (item.stacks == null) {
            item.stacks = Lists.newArrayList();
            item.stacksView = Collections.unmodifiableList(item.stacks);
        }
        for (Item i : items) {
            item.stacks.add(new ItemStack(i, 1, 0));
        }
        return item;
    }

    public AbstractStack with(Block... blocks) {
        AbstractStack item = new AbstractStack(this);
        if (item.stacks == null) {
            item.stacks = Lists.newArrayList();
            item.stacksView = Collections.unmodifiableList(item.stacks);
        }
        for (Block block : blocks) {
            item.stacks.add(new ItemStack(block, 1, 0));
        }
        return item;
    }

    public AbstractStack with(String... oreDictNames) {
        AbstractStack item = new AbstractStack(this);
        if (item.oreDictEntries == null) {
            item.oreDictEntries = Sets.newHashSet();
        }
        for (String oreDictName : oreDictNames) {
            int id = OreDictionary.getOreID(oreDictName);
            item.oreDictEntries.add(id);
            OREDICT_ENTRIES.computeIfAbsent(id, n -> OreDictionary.getOres(oreDictName));
        }
        return item;
    }

    /**
     * Creates an {@link AbstractStack} that doesn't match any items.
     */
    public static AbstractStack empty() {
        return EMPTY;
    }

    /**
     * Creates an {@link AbstractStack} that matches the specified {@link ItemStack}s.
     */
    public static AbstractStack of(ItemStack... stacks) {
        return empty().with(stacks);
    }

    /**
     * Creates an {@link AbstractStack} that matches the specified {@link Item}s.
     */
    public static AbstractStack of(Item... items) {
        return empty().with(items);
    }

    /**
     * Creates an {@link AbstractStack} that matches the specified {@link Block}s.
     */
    public static AbstractStack of(Block... blocks) {
        return empty().with(blocks);
    }

    /**
     * Creates an {@link AbstractStack} that matches the specified {@link OreDictionary} entries.
     */
    public static AbstractStack of(String... oreDictNames) {
        return empty().with(oreDictNames);
    }

}

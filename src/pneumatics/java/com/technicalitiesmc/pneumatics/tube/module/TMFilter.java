package com.technicalitiesmc.pneumatics.tube.module;

import com.technicalitiesmc.api.pneumatics.EnumTubeDirection;
import com.technicalitiesmc.api.pneumatics.IPneumaticTube;
import com.technicalitiesmc.api.pneumatics.ITubeStack;
import com.technicalitiesmc.api.pneumatics.TubeModule;
import com.technicalitiesmc.lib.inventory.widget.WidgetGhostSlot;
import com.technicalitiesmc.pneumatics.TKPneumatics;
import com.technicalitiesmc.pneumatics.inventory.WidgetColorSwitcher;
import com.technicalitiesmc.pneumatics.tube.IWindowModule;
import elec332.core.inventory.window.IWindowModifier;
import elec332.core.inventory.window.Window;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TMFilter extends TubeModule implements IWindowModule, IColorCycler, IWindowModifier {

    private static final ResourceLocation PATH_SINGLE = new ResourceLocation(TKPneumatics.MODID, "block/tube/filter/single");
    // private static final ResourceLocation PATH_MASTER = new ResourceLocation(TKPneumatics.MODID, "block/tube/filter/single");
    // private static final ResourceLocation PATH_SLAVE = new ResourceLocation(TKPneumatics.MODID, "block/tube/filter/single");

    private TMFilter other;
    private boolean master;

    private final FilterInventory filterInv;
    private Filter[] filters;
    private EnumDyeColor[] color = { null, null };

    public TMFilter(IPneumaticTube tube, EnumFacing side) {
        this(tube, side, true, 4);
    }

    public TMFilter(IPneumaticTube tube, EnumFacing side, boolean master) {
        this(tube, side, master, 8);
    }

    private TMFilter(IPneumaticTube tube, EnumFacing side, boolean master, int slots) {
        super(tube, side);
        this.master = master;
        initFilters(slots);
        this.filterInv = new FilterInventory();
    }

    private void initFilters(int slots) {
        this.filters = new Filter[slots];
        Arrays.setAll(this.filters, i -> new Filter());
    }

    @Override
    public boolean requiresConnection() {
        return true;
    }

    @Override
    public boolean preventsConnection() {
        return false;
    }

    @Override
    public boolean renderTube() {
        return false;
    }

    @Override
    public ResourceLocation getModel() {
        return PATH_SINGLE;
    }

    @Override
    public boolean canStackTraverse(ITubeStack stack, EnumTubeDirection direction) {
        if (!master) {
            return direction == EnumTubeDirection.INWARDS;
        }
        for (Filter filter : filters) {
            if (filter.test(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ITubeStack traverse(ITubeStack stack, EnumTubeDirection direction) {
        if (master && color != null) {
            int i = 0;
            for (Filter filter : filters) {
                if (filter.test(stack)) {
                    break;
                }
                i++;
            }
            if (i < 4) {
                return stack.withColor(color[0]);
            } else if (i < 8) {
                return stack.withColor(color[1]);
            }
        }
        return stack;
    }

    @Override
    public boolean onActivated(EntityPlayer player, EnumHand hand) {
        if (!master && other != null) {
            return other.onActivated(player, hand);
        }
        if (!getTube().getTubeWorld().isRemote) {
            openWindow.accept(this, player);
        }
        return true;
    }

    @Override
    public Window createWindow(Object... objects) {
        boolean big = other != null;
        return new Window(176, big ? 174 : 133, this);
    }

    @Override
    public void modifyWindow(Window window, Object... objects) {
        boolean big = other != null;
        window.setBackground(new ResourceLocation(TKPneumatics.MODID, "textures/gui/tube_module/filter" + (big ? 8 : 4) + ".png"));
        int offsetMin = -84;
        offsetMin += big ? 92 : 51;
        window.setOffset(offsetMin);
        for (int i = 0; i < filters.length; i++) {
            window.addWidget(new WidgetGhostSlot(filterInv, i, 53 + (i % 4) * 18, 13 + (i >= 4 ? 52 : 0), false));
        }
        window.addPlayerInventoryToContainer();
        for (int j = 0; j < (big ? 2 : 1); j++) {
            for (int i = 0; i < 4; i++) {
                int id = i + j * 4;
                int x = 57 + i * 18, y = 32 + j * 22;
                window.addWidget(new WidgetColorSwitcher(x, y, this, id));
            }
            int x = 147, y = 24 + j * 38;
            window.addWidget(new WidgetColorSwitcher(x, y, this, -1 - j));
        }
    }

    @Override
    public EnumDyeColor getColor(int id) {
        if (id >= 0) {
            return filters[id].color;
        } else if (id == -1) {
            return this.color[0];
        } else if (id == -2) {
            return this.color[1];
        } else {
            return null;
        }
    }

    @Override
    public void cycleColor(int id, boolean backwards) {
        EnumDyeColor prevColor = getColor(id);
        int meta = (prevColor != null ? prevColor.getMetadata() : 16) + (backwards ? -1 : 1);
        if (meta < 0) {
            meta += 17;
        }
        meta = meta % 17;
        EnumDyeColor color = meta < 16 ? EnumDyeColor.byMetadata(meta) : null;
        if (id >= 0) {
            filters[id].color = color;
        } else if (id == -1) {
            this.color[0] = color;
        } else if (id == -2) {
            this.color[1] = color;
        }
        save();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);

        tag.setBoolean("master", master);

        NBTTagList filters = new NBTTagList();
        for (Filter filter : this.filters) {
            NBTTagCompound t = new NBTTagCompound();
            t.setTag("stack", filter.stack.serializeNBT());
            t.setBoolean("meta", filter.matchMeta);
            t.setBoolean("nbt", filter.matchNBT);
            t.setBoolean("ore", filter.matchOredict);
            if (filter.color != null) {
                t.setByte("color", (byte) filter.color.getMetadata());
            }
            t.setBoolean("uncolored", filter.allowUncolored);
            filters.appendTag(t);
        }
        tag.setTag("filters", filters);

        for (int i = 0; i < color.length; i++) {
            if (color[i] != null) {
                tag.setByte("color" + i, (byte) color[i].getMetadata());
            }
        }

        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        master = tag.getBoolean("master");

        NBTTagList filters = tag.getTagList("filters", NBT.TAG_COMPOUND);
        initFilters(filters.tagCount());
        int i = 0;
        for (Filter filter : this.filters) {
            NBTTagCompound t = filters.getCompoundTagAt(i++);
            filter.stack = new ItemStack(t.getCompoundTag("stack"));
            filter.matchMeta = t.getBoolean("meta");
            filter.matchNBT = t.getBoolean("nbt");
            filter.matchOredict = t.getBoolean("ore");
            if (t.hasKey("color")) {
                filter.color = EnumDyeColor.byMetadata(t.getByte("color") & 0xFF);
            } else {
                filter.color = null;
            }
            filter.allowUncolored = t.getBoolean("uncolored");
        }

        for (i = 0; i < color.length; i++) {
            if (tag.hasKey("color" + i)) {
                color[i] = EnumDyeColor.byMetadata(tag.getByte("color" + i) & 0xFF);
            } else {
                color[i] = null;
            }
        }
    }

    @Override
    public NBTTagCompound writeDescription(NBTTagCompound tag) {
        return writeToNBT(super.writeDescription(tag));
    }

    @Override
    public void readDescription(NBTTagCompound tag) {
        super.readDescription(tag);
        readFromNBT(tag);
    }

    @Override
    public void writeUpdateExtra(PacketBuffer buf) {
        super.writeUpdateExtra(buf);
        buf.writeBoolean(master);
        buf.writeInt(filters.length);
        for (int i = 0; i < filters.length; i++) {
            Filter f = filters[i];
            if (f.color != null) {
                buf.writeBoolean(true);
                buf.writeEnumValue(f.color);
            } else {
                buf.writeBoolean(false);
            }
        }
        if (color[0] != null) {
            buf.writeBoolean(true);
            buf.writeEnumValue(color[0]);
        } else {
            buf.writeBoolean(false);
        }
        if (color[1] != null) {
            buf.writeBoolean(true);
            buf.writeEnumValue(color[1]);
        } else {
            buf.writeBoolean(false);
        }
    }

    @Override
    public void readUpdateExtra(PacketBuffer buf) {
        super.readUpdateExtra(buf);
        master = buf.readBoolean();
        int filterCount = buf.readInt();
        if (filters.length != filterCount) {
            initFilters(filterCount);
        }
        for (int i = 0; i < filters.length; i++) {
            Filter f = filters[i];
            f.color = buf.readBoolean() ? buf.readEnumValue(EnumDyeColor.class) : null;
        }
        color[0] = buf.readBoolean() ? buf.readEnumValue(EnumDyeColor.class) : null;
        color[1] = buf.readBoolean() ? buf.readEnumValue(EnumDyeColor.class) : null;
    }

    private class Filter implements Predicate<ITubeStack> {

        private ItemStack stack = ItemStack.EMPTY;
        private boolean matchMeta = true;
        private boolean matchNBT = true;
        private boolean matchOredict = false;

        private EnumDyeColor color = null;
        private boolean allowUncolored = true;

        @Override
        public boolean test(ITubeStack stack) {
            EnumDyeColor stackColor = stack.getColor();
            if (color != null && stackColor != color && (!allowUncolored || stackColor != null)) {
                return false;
            }

            if (this.stack.isEmpty()) {
                return color != null;
            }

            ItemStack item = stack.getStack();
            if (item.getItem() != this.stack.getItem()) {
                return false;
            }
            if (matchMeta && item.getMetadata() != this.stack.getMetadata()) {
                return false;
            }
            if (matchNBT && !ItemStack.areItemStackTagsEqual(item, this.stack)) {
                return false;
            }
            if (matchOredict && !Arrays.stream(OreDictionary.getOreIDs(this.stack)).anyMatch(i -> ArrayUtils.contains(OreDictionary.getOreIDs(item), i))) {
                return false; // TODO: This can obviously be optimized in some way I don't know about yet...
            }

            return true;
        }

    }

    private class FilterInventory implements IItemHandlerModifiable {

        @Override
        public int getSlots() {
            return filters.length;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return filters[slot].stack;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!simulate) {
                stack = stack.copy();
                stack.setCount(1);
                filters[slot].stack = stack;
            }
            return null;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack stack = filters[slot].stack;
            if (!simulate) {
                filters[slot].stack = ItemStack.EMPTY;
            }
            return stack;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            filters[slot].stack = stack;
        }

    }

    public static class Type extends TubeModule.Type<TMFilter> {

        @Override
        public TMFilter placeSingle(IPneumaticTube tube, EnumFacing side) {
            return new TMFilter(tube, side);
        }

        @Override
        public Pair<TMFilter, TMFilter> placePair(IPneumaticTube tube, EnumFacing side, IPneumaticTube other) {
            TMFilter filter1 = new TMFilter(tube, side, false);
            TMFilter filter2 = new TMFilter(other, side.getOpposite(), true);
            link(filter1, filter2);
            return Pair.of(filter1, filter2);
        }

        @Override
        public TMFilter instantiate(IPneumaticTube tube, EnumFacing side) {
            return new TMFilter(tube, side);
        }

        @Override
        public void link(TMFilter filter1, TMFilter filter2) {
            filter1.other = filter2;
            filter2.other = filter1;
        }

        @Override
        public void registerModels(Consumer<ResourceLocation> registry) {
            registry.accept(PATH_SINGLE);
        }

    }

}

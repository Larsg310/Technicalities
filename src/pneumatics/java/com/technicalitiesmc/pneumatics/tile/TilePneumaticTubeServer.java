package com.technicalitiesmc.pneumatics.tile;

import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.technicalitiesmc.api.pneumatics.EnumTubeDirection;
import com.technicalitiesmc.api.pneumatics.ITubeStack;
import com.technicalitiesmc.api.pneumatics.TubeModule;
import com.technicalitiesmc.pneumatics.tube.TubeStack;
import com.technicalitiesmc.pneumatics.tube.TubeTicker;
import com.technicalitiesmc.util.inventory.SimpleItemHandler;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TilePneumaticTubeServer extends TilePneumaticTubeBase {

    private final Map<EnumFacing, Neighbor> neighbors = new EnumMap<>(EnumFacing.class);
    private final Map<EnumFacing, Inventory> inventories = new EnumMap<>(EnumFacing.class);

    public TilePneumaticTubeServer() {
        for (EnumFacing face : EnumFacing.VALUES) {
            inventories.put(face, new Inventory());
        }
    }

    @Override
    public <T extends TubeModule> boolean setModule(EnumFacing face, TubeModule.Type<T> type) {
        if (type == null) {
            Triple<TubeModule.Type<?>, TubeModule, TubeModule> module = modules.remove(face);
            markDirty();
            if (module != null && module.getRight() != null) {
                TilePneumaticTubeServer tube = (TilePneumaticTubeServer) module.getRight().getTube();
                tube.modules.remove(face.getOpposite());
                tube.markDirty();
            }
            return true;
        }
        if (isConnected(face) && getNeighbor(face).isTube()) { // Dual module
            TilePneumaticTubeServer tube = getNeighbor(face).asTube();

            Pair<T, T> pair = type.placePair(this, face, tube);

            if (pair.getRight() != null && tube.getModule(face.getOpposite()) != null) {
                return false; // There's something in the way!
            }

            modules.put(face, Triple.of(type, pair.getLeft(), pair.getRight()));
            if (pair.getRight() != null) {
                tube.modules.put(face.getOpposite(), Triple.of(type, pair.getRight(), pair.getLeft()));
            }

            if (pair.getLeft().preventsConnection() || (pair.getRight() != null && pair.getRight().preventsConnection())) {
                neighbors.remove(face);
                tube.neighbors.remove(face.getOpposite());
            }

            markDirty();
            tube.markDirty();

            return true;
        } else { // Regular module
            TubeModule module = type.placeSingle(this, face);
            if (module.requiresConnection() && !isConnected(face)) {
                return false;
            }
            modules.put(face, Triple.of(type, module, null));

            if (module.preventsConnection()) {
                neighbors.remove(face);
            }

            markDirty();

            return true;
        }
    }

    public void computeNeighbor(EnumFacing side) {
        if (side == null) {
            for (EnumFacing f : EnumFacing.VALUES) {
                computeNeighbor(f);
            }
        } else {
            Triple<?, TubeModule, TubeModule> module = modules.get(side);
            if (module == null || !module.getMiddle().preventsConnection()) {
                TileEntity te = getWorld().getTileEntity(pos.offset(side));
                if (te != null) {
                    if (te.hasCapability(CAPABILITY, null)) {
                        TilePneumaticTubeServer tube = (TilePneumaticTubeServer) te.getCapability(CAPABILITY, null);
                        Triple<?, TubeModule, TubeModule> other = tube.modules.get(side.getOpposite());
                        if (other == null || !other.getMiddle().preventsConnection()) {
                            neighbors.put(side, new Neighbor((TilePneumaticTubeServer) te.getCapability(CAPABILITY, null)));
                            markDirty();
                            return;
                        }
                    } else if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite())) {
                        neighbors.put(side,
                                new Neighbor(te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite())));
                        markDirty();
                        return;
                    }
                }
            }
            if (neighbors.remove(side) != null) {
                if (module != null && module.getMiddle().requiresConnection()) {
                    if (module.getRight() == null) {
                        // TODO: Spawn item
                    }
                    modules.remove(side);
                }
                markDirty();
            }
        }
    }

    @Override
    public Neighbor getNeighbor(EnumFacing side) {
        return neighbors.get(side);
    }

    @Override
    public boolean isConnected(EnumFacing side) {
        return getNeighbor(side) != null;
    }

    @Override
    public void insertStack(EnumFacing side, float position, EnumTubeDirection direction, ItemStack stack, EnumDyeColor color) {
        getWorld().getCapability(TubeTicker.CAPABILITY, null).addStack(this, new TubeStack(this, side, position, direction, stack, color));
        markDirty();
    }

    @Override
    public void removeStack(ITubeStack stack) {
        getWorld().getCapability(TubeTicker.CAPABILITY, null).removeStack(this, (TubeStack) stack);
        markDirty();
    }

    public void dumpInventories() {
        inventories.forEach((face, inv) -> {
            ItemStack stack = inv.getStackInSlot(0);
            if (!stack.isEmpty()) {
                insertStack(face, 0, EnumTubeDirection.INWARDS, stack, null);
            }
            inv.setStackInSlot(0, ItemStack.EMPTY);
        });
    }

    private void onInventoryStackInserted() {
        getWorld().getCapability(TubeTicker.CAPABILITY, null).scheduleInventoryDump(this);
        markDirty();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        getWorld().scheduleUpdate(getPos(), getBlockType(), 0);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null && isConnected(facing)) {
            TubeModule mod = getModule(facing);
            return mod == null || !mod.preventsConnection();
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null && isConnected(facing)) {
            return (T) inventories.get(facing);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void writeDescription(PacketBuffer buf) {
        super.writeDescription(buf);

        int connections = 0;
        for (EnumFacing face : neighbors.keySet()) {
            connections |= 1 << face.ordinal();
        }
        buf.writeInt(connections);
    }

    // Used to limit insertion rates to just 1 stack per tick per face
    private class Inventory extends SimpleItemHandler {

        public Inventory() {
            super(1, TilePneumaticTubeServer.this::onInventoryStackInserted);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

    }

    public class Neighbor implements INeighbor {

        private final IItemHandler inventory;
        private final TilePneumaticTubeServer tube;

        public Neighbor(IItemHandler inventory) {
            this.inventory = inventory;
            this.tube = null;
        }

        public Neighbor(TilePneumaticTubeServer tube) {
            this.inventory = null;
            this.tube = tube;
        }

        @Override
        public boolean isInventory() {
            return inventory != null;
        }

        @Override
        public boolean isTube() {
            return tube != null;
        }

        @Override
        public IItemHandler asInventory() {
            if (!isInventory()) {
                throw new IllegalStateException("Neighbor is not an inventory.");
            }
            return inventory;
        }

        @Override
        public TilePneumaticTubeServer asTube() {
            if (!isTube()) {
                throw new IllegalStateException("Neighbor is not a tube.");
            }
            return tube;
        }

    }

}

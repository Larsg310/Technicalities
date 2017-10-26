package com.technicalitiesmc.pneumatics.tube;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.pneumatics.TKPneumatics;
import com.technicalitiesmc.pneumatics.network.PacketStackJoinNetwork;
import com.technicalitiesmc.pneumatics.network.PacketStackLeaveNetwork;
import com.technicalitiesmc.pneumatics.tile.TilePneumaticTubeBase;
import com.technicalitiesmc.pneumatics.tile.TilePneumaticTubeClient;
import com.technicalitiesmc.pneumatics.tile.TilePneumaticTubeServer;
import com.technicalitiesmc.pneumatics.tile.TilePneumaticTubeServer.Neighbor;
import com.technicalitiesmc.util.simple.SimpleCapability;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.minecraft.block.Block;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@SimpleCapability // TODO: Replace with a proper capability registration method (to save to the world)
public class TubeTicker {

    @CapabilityInject(TubeTicker.class)
    public static final Capability<TubeTicker> CAPABILITY = null;
    private static final ResourceLocation CAP_NAME = new ResourceLocation(TKPneumatics.MODID, "tube_ticker");

    @SubscribeEvent
    public static void onWorldCreate(AttachCapabilitiesEvent<World> event) {
        TubeTicker ticker = new TubeTicker();
        event.addCapability(CAP_NAME, new ICapabilityProvider() {

            @Override
            public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
                return capability == CAPABILITY;
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
                return capability == CAPABILITY ? (T) ticker : null;
            }
        });
        MinecraftForge.EVENT_BUS.register(ticker);
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        MinecraftForge.EVENT_BUS.unregister(event.getWorld().getCapability(CAPABILITY, null));
    }

    // Server

    private final Set<TubeStack> serverStacks = new HashSet<>();
    private final Set<TilePneumaticTubeServer> scheduledInventoryDumps = new HashSet<>();
    private long tubeID = 0;

    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {
        if (event.phase == Phase.START) {
            return;
        }

        // Dump tube inventories into the stack tracker
        scheduledInventoryDumps.forEach(TilePneumaticTubeServer::dumpInventories);
        scheduledInventoryDumps.clear();

        // Tick stacks as needed and move them along their respective tubes
        Iterator<TubeStack> it = serverStacks.iterator();
        int i = 0, total = serverStacks.size();
        while (i < total && it.hasNext()) {
            i++;

            TubeStack stack = it.next();
            TilePneumaticTubeServer tube = (TilePneumaticTubeServer) stack.getTube();
            if (stack.update(null)) {
                Block.spawnAsEntity(tube.getWorld(), tube.getPos(), stack.getStack());
                it.remove();
                tube.stacks.remove(stack);
                tube.markDirty();
                notifyStackRemoval(stack);
                continue;
            }

            float progress = stack.getProgress();
            Neighbor neighbor = tube.getNeighbor(stack.getTo());
            if (progress >= 0.5F && neighbor == null) {
                Block.spawnAsEntity(tube.getWorld(), tube.getPos(), stack.getStack());
                it.remove();
                tube.stacks.remove(stack);
                tube.markDirty();
                notifyStackRemoval(stack);
            } else if (progress >= 1.0F) {
                if (neighbor.isTube()) {
                    stack.transferTo(neighbor.asTube());
                } else {
                    ItemStack extra = ItemHandlerHelper.insertItemStacked(neighbor.asInventory(), stack.getStack(), false);
                    if (!extra.isEmpty()) {
                        Block.spawnAsEntity(tube.getWorld(), tube.getPos(), extra);
                    }
                    it.remove();
                    tube.stacks.remove(stack);
                    tube.markDirty();
                    notifyStackRemoval(stack);
                }
            }
        }
    }

    private void notifyStackAddition(TubeStack stack) {
        TKPneumatics.NETWORK_HANDLER.sendToAllAround(new PacketStackJoinNetwork(stack), stack.getTube().getWorld(),
                stack.getTube().getPos());
    }

    private void notifyStackRemoval(TubeStack stack) {
        TKPneumatics.NETWORK_HANDLER.sendToAllAround(new PacketStackLeaveNetwork(stack), stack.getTube().getWorld(),
                stack.getTube().getPos());
    }

    public void scheduleInventoryDump(TilePneumaticTubeServer tube) {
        scheduledInventoryDumps.add(tube);
    }

    public void addStack(TilePneumaticTubeServer tube, TubeStack stack) {
        stack.setID(tubeID++);
        serverStacks.add(stack);
        tube.stacks.add(stack);
        notifyStackAddition(stack);
    }

    public void removeStack(TilePneumaticTubeServer tube, TubeStack stack) {
        serverStacks.remove(stack);
        tube.stacks.remove(stack);
    }

    public void load(TilePneumaticTubeServer tube) {
        serverStacks.addAll(tube.stacks);
    }

    public void unload(TilePneumaticTubeServer tube) {
        serverStacks.removeAll(tube.stacks);
    }

    // Client

    private final Set<TubeStack> clientStacks = new HashSet<>();
    private final TLongObjectMap<Map<BlockPos, EnumFacing>> nextDirections = new TLongObjectHashMap<>();
    private final TLongObjectMap<Pair<ItemStack, EnumDyeColor>> stackInfo = new TLongObjectHashMap<>();

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if (event.phase == Phase.START || Technicalities.proxy.isGamePaused()) {
            return;
        }

        // Tick stacks as needed and move them along their respective tubes
        Iterator<TubeStack> it = clientStacks.iterator();
        int i = 0, total = clientStacks.size();
        while (i < total && it.hasNext()) {
            i++;

            TubeStack stack = it.next();
            TilePneumaticTubeClient tube = (TilePneumaticTubeClient) stack.getTube();

            Pair<ItemStack, EnumDyeColor> info = stackInfo.remove(stack.getID());
            if (info != null) {
                stack.update(info.getKey(), info.getValue());
            }

            Map<BlockPos, EnumFacing> directions = nextDirections.get(stack.getID());
            if (stack.update(() -> directions.remove(tube.getPos()))) {
                it.remove();
                tube.stacks.remove(stack);
                continue;
            }

            float progress = stack.getProgress();
            if (progress >= 1.0F) {
                TileEntity te = tube.getWorld().getTileEntity(tube.getPos().offset(stack.getTo()));
                if (te != null && te.hasCapability(TilePneumaticTubeBase.CAPABILITY, null)) {
                    stack.transferTo(te.getCapability(TilePneumaticTubeBase.CAPABILITY, null));
                }
            }
        }
    }

    public void addStack(TilePneumaticTubeClient tube, TubeStack stack) {
        clientStacks.add(stack);
        tube.stacks.add(stack);
        nextDirections.putIfAbsent(stack.getID(), new HashMap<>()); // TODO: Shouldn't always create an instance
    }

    public void removeStack(long id) {
        Iterator<TubeStack> it = clientStacks.iterator();
        TubeStack stack;
        while (it.hasNext()) {
            stack = it.next();
            if (stack.getID() == id) {
                it.remove();
                stack.getTube().stacks.remove(stack);
                nextDirections.remove(id);
                return;
            }
        }
    }

    public void specifyDirection(long id, BlockPos pos, EnumFacing direction) {
        Map<BlockPos, EnumFacing> directions = nextDirections.get(id);
        if (directions == null) {
            nextDirections.put(id, directions = new HashMap<>());
        }
        directions.put(pos, direction);
    }

    public void update(long id, ItemStack stack, EnumDyeColor color) {
        stackInfo.put(id, Pair.of(stack, color));
    }

}

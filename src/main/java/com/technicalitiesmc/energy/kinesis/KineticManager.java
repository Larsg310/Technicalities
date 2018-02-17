package com.technicalitiesmc.energy.kinesis;

import com.technicalitiesmc.api.mechanical.IGearAttachable;
import com.technicalitiesmc.api.mechanical.IShaftAttachable;
import com.technicalitiesmc.lib.simple.SimpleCapability;
import gnu.trove.map.TObjectFloatMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;

public interface KineticManager {

    // Used to register simple capabilities
    @SimpleCapability
    @CapabilityInject(IShaftAttachable.class)
    Capability<IShaftAttachable> CAP_SHAFT_ATTACHABLE = null;
    @SimpleCapability
    @CapabilityInject(IGearAttachable.class)
    Capability<IGearAttachable> CAP_GEAR_ATTACHABLE = null;

    static void init() {
        MinecraftForge.EVENT_BUS.register(ServerKineticManager.INSTANCE);
        MinecraftForge.EVENT_BUS.register(ClientKineticManager.INSTANCE);
    }

    default void visitNetwork(KineticNode start, BiPredicate<World, BlockPos> posValidator) {
        Queue<KineticNode> activeQueue = new ArrayDeque<>();
        activeQueue.add(start);
        while (!activeQueue.isEmpty()) {
            KineticNode current = activeQueue.poll();
            current.markVisited();
            current.addNeighbors((node, ratio) -> {
                if (node != null && !((KineticNode) node).isVisited()) {
                    activeQueue.add((KineticNode) node);
                }
            }, posValidator);
        }
    }

    default boolean findNetwork(TObjectFloatMap<KineticNode> nodes, KineticNode activeNode, BiPredicate<World, BlockPos> posValidator) {
        AtomicBoolean failed = new AtomicBoolean(false);

        Queue<Pair<KineticNode, Float>> activeQueue = new ArrayDeque<>();
        activeQueue.add(Pair.of(activeNode, 1F));
        while (!activeQueue.isEmpty()) {
            Pair<KineticNode, Float> pair = activeQueue.poll();
            float prevRatio = nodes.put(pair.getKey(), pair.getValue());

            if (Float.isNaN(prevRatio)) {
                pair.getKey().setRatio(pair.getValue());
                pair.getKey().addNeighbors((node, ratio) -> activeQueue.add(Pair.of((KineticNode) node, ratio * pair.getValue())), posValidator);
            } else if (prevRatio != pair.getValue()) {
                pair.getKey().setRatio(0);
                failed.set(true);
            }
            pair.getKey().markVisited();
        }

        return !failed.get();
    }

}

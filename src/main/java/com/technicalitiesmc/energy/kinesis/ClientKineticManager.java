package com.technicalitiesmc.energy.kinesis;

import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.hash.TObjectFloatHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

public enum ClientKineticManager implements KineticManager {
    INSTANCE;

    private final Set<KineticNode> nodes = Collections.newSetFromMap(new WeakHashMap<>());
    final Object2FloatMap<UUID> clientUpdates = new Object2FloatOpenHashMap<>();

    public void add(KineticNode node) {
        nodes.add(node);
    }

    public void remove(KineticNode node) {
        nodes.remove(node);
    }

    public void onUpdate(UUID node, float velocity){
        clientUpdates.put(node, velocity);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != Phase.END) return;

        nodes.forEach(KineticNode::resetVisitState);

        Queue<KineticNode> queue = new ArrayDeque<>(nodes);
        while (!queue.isEmpty()) {
            KineticNode activeNode = queue.poll();
            if (activeNode.isVisited()) continue;

            float velocity = clientUpdates.getOrDefault(activeNode.getNodeID(), Float.NaN);
            if (Float.isNaN(velocity)) {
                continue;
            }

            TObjectFloatMap<KineticNode> nodes = new TObjectFloatHashMap<>(16, 0.75F, Float.NaN);
            if (findNetwork(nodes, activeNode, World::isBlockLoaded)) {
                updateNetwork(nodes, velocity);
            } else {
                nodes.keySet().forEach(node -> node.update(Float.NaN, 0, Float.NaN));
            }
        }

        clientUpdates.clear();
    }

    private void updateNetwork(TObjectFloatMap<KineticNode> nodes, float totalVelocity) {
        nodes.forEachEntry((node, ratio) -> {
            node.update(Float.NaN, totalVelocity / ratio, Float.NaN);
            return true;
        });
    }

}

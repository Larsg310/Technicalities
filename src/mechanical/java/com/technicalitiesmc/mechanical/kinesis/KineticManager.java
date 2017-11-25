package com.technicalitiesmc.mechanical.kinesis;

import com.google.common.util.concurrent.AtomicDouble;
import com.technicalitiesmc.api.mechanical.IKineticNode;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.hash.TObjectFloatHashMap;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public enum KineticManager {
    INSTANCE;

    private final Set<KineticNode> nodes = new HashSet<>();

    public void add(IKineticNode node) {
        nodes.add((KineticNode) node);
    }

    public void remove(IKineticNode node) {
        nodes.remove(node);
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {
        if (event.phase == Phase.END) {
            tick();
        }
    }

    private void tick() {
        nodes.forEach(KineticNode::resetVisitState);

        Queue<KineticNode> queue = new ArrayDeque<>(nodes);
        while (!queue.isEmpty()) {
            KineticNode activeNode = queue.poll();
            if (activeNode.isVisited()) {
                continue;
            }

            TObjectFloatMap<KineticNode> nodes = new TObjectFloatHashMap<>(16, 0.75F, 0);
            if (findNetwork(nodes, activeNode)) {
                updateNetwork(nodes);
            } else {
                nodes.keySet().forEach(node -> node.update(0, 0, 0));
            }
        }
    }

    private boolean findNetwork(TObjectFloatMap<KineticNode> nodes, KineticNode activeNode) {
        Queue<Pair<KineticNode, Float>> activeQueue = new ArrayDeque<>();
        activeQueue.add(Pair.of(activeNode, 1F));
        nodes.put(activeNode, 1);
        AtomicBoolean failed = new AtomicBoolean(false);

        while (!activeQueue.isEmpty()) {
            Pair<KineticNode, Float> pair = activeQueue.poll();
            pair.getKey().markVisited();
            pair.getKey().addNeighbors((node, ratio) -> {
                if (node == null) {
                    return;
                }
                float prevRatio = nodes.putIfAbsent((KineticNode) node, ratio * pair.getValue());
                if (prevRatio == 0) {
                    activeQueue.add(Pair.of((KineticNode) node, ratio * pair.getValue()));
                } else if (prevRatio != ratio) {
                    failed.set(true);
                }
            });
        }

        return !failed.get();
    }

    private void updateNetwork(TObjectFloatMap<KineticNode> nodes) {
        AtomicDouble totalPower = new AtomicDouble(0);
        AtomicDouble totalInertia = new AtomicDouble(0);

        nodes.forEachEntry((node, ratio) -> {
            totalPower.addAndGet(node.getPower() + node.getAppliedPower() - node.getConsumedPower());
            totalInertia.addAndGet(node.getInertia() * ratio * ratio);
            return true;
        });

        if (totalPower.get() <= 0) {
            nodes.keySet().forEach(node -> node.update(0, 0, 0));
            return;
        }

        float totalVelocitySq = (float) (totalPower.get() / totalInertia.get());

        nodes.forEachEntry((node, ratio) -> {
            float power = node.getInertia() * totalVelocitySq / (ratio * ratio);
            float velocity = (float) Math.sqrt(totalVelocitySq) / ratio;
            float torque = power / velocity;

            node.update(torque, velocity, power);
            return true;
        });
    }

}

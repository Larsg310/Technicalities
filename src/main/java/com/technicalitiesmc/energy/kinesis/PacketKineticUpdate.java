package com.technicalitiesmc.energy.kinesis;

import com.technicalitiesmc.base.Technicalities;
import com.technicalitiesmc.lib.network.Packet;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Collection;
import java.util.UUID;

public class PacketKineticUpdate extends Packet<PacketKineticUpdate> {

    private final Object2FloatMap<UUID> updates = new Object2FloatOpenHashMap<>();
    private Collection<KineticNode> nodes;

    public PacketKineticUpdate(Collection<KineticNode> nodes) {
        this.nodes = nodes;
    }

    public PacketKineticUpdate() {
    }

    @Override
    public void handleClientSide(EntityPlayer player) {
        Technicalities.proxy.schedule(Side.CLIENT, () -> ClientKineticManager.INSTANCE.clientUpdates.putAll(updates));
    }

    @Override
    public void handleServerSide(EntityPlayer player) {
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeInt(nodes.size());
        for (KineticNode node : nodes) {
            buf.writeUniqueId(node.getNodeID());
            buf.writeFloat(node.getVelocity());
        }
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            updates.put(buf.readUniqueId(), buf.readFloat());
        }
    }

}

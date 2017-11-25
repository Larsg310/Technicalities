package com.technicalitiesmc.pneumatics.network;

import com.technicalitiesmc.pneumatics.tube.TubeStack;
import com.technicalitiesmc.pneumatics.tube.TubeTicker;
import com.technicalitiesmc.util.network.Packet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

public class PacketStackLeaveNetwork extends Packet<PacketStackLeaveNetwork> {

    private long id;

    public PacketStackLeaveNetwork(TubeStack stack) {
        this.id = stack.getID();
    }

    public PacketStackLeaveNetwork() {
    }

    @Override
    public void handleClientSide(EntityPlayer player) {
        player.world.getCapability(TubeTicker.CAPABILITY, null).removeStack(id);
    }

    @Override
    public void handleServerSide(EntityPlayer player) {

    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeLong(id);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        id = buf.readLong();
    }

}

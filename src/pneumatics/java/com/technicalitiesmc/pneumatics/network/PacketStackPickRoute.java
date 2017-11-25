package com.technicalitiesmc.pneumatics.network;

import com.technicalitiesmc.pneumatics.tube.TubeStack;
import com.technicalitiesmc.pneumatics.tube.TubeTicker;
import com.technicalitiesmc.util.network.Packet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class PacketStackPickRoute extends Packet<PacketStackPickRoute> {

    private TubeStack stack;

    private long id;
    private BlockPos pos;
    private EnumFacing direction;

    public PacketStackPickRoute(TubeStack stack) {
        this.stack = stack;
    }

    public PacketStackPickRoute() {
    }

    @Override
    public void handleClientSide(EntityPlayer player) {
        player.world.getCapability(TubeTicker.CAPABILITY, null).specifyDirection(id, pos, direction);
    }

    @Override
    public void handleServerSide(EntityPlayer player) {

    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeLong(stack.getID());
        buf.writeBlockPos(stack.getTube().getPos());
        buf.writeEnumValue(stack.getTo());
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        id = buf.readLong();
        pos = buf.readBlockPos();
        direction = buf.readEnumValue(EnumFacing.class);
    }

}

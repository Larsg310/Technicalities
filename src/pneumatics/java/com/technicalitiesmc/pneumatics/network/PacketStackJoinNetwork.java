package com.technicalitiesmc.pneumatics.network;

import com.technicalitiesmc.pneumatics.tile.TilePneumaticTubeBase;
import com.technicalitiesmc.pneumatics.tile.TilePneumaticTubeClient;
import com.technicalitiesmc.pneumatics.tube.TubeStack;
import com.technicalitiesmc.pneumatics.tube.TubeTicker;
import com.technicalitiesmc.util.network.Packet;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class PacketStackJoinNetwork extends Packet<PacketStackJoinNetwork> {

    private TubeStack stack;

    private BlockPos pos;
    private PacketBuffer data;

    public PacketStackJoinNetwork(TubeStack stack) {
        this.stack = stack;
    }

    public PacketStackJoinNetwork() {
    }

    @Override
    public void handleClientSide(EntityPlayer player) {
        TileEntity te = player.world.getTileEntity(pos);
        if (te != null && te.hasCapability(TilePneumaticTubeBase.CAPABILITY, null)) {
            TilePneumaticTubeClient tube = (TilePneumaticTubeClient) te.getCapability(TilePneumaticTubeBase.CAPABILITY, null);
            stack = TubeStack.fromBytes(tube, data);
            player.world.getCapability(TubeTicker.CAPABILITY, null).addStack(tube, stack);
        }
    }

    @Override
    public void handleServerSide(EntityPlayer player) {

    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(stack.getTube().getPos());

        PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        stack.toBytes(data);
        buf.writeByteArray(data.array());
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        pos = buf.readBlockPos();

        data = new PacketBuffer(Unpooled.copiedBuffer(buf.readByteArray()));
    }

}

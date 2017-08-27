package com.technicalitiesmc.pneumatics.network;

import com.technicalitiesmc.pneumatics.tile.TilePneumaticTubeBase;
import com.technicalitiesmc.util.network.LocatedPacket;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class PacketModuleInfo extends LocatedPacket<PacketModuleInfo> {

    private EnumFacing side;
    private PacketBuffer data;

    public PacketModuleInfo(BlockPos pos, EnumFacing side, PacketBuffer data) {
        super(pos);
        this.side = side;
        this.data = data;
    }

    public PacketModuleInfo() {
    }

    @Override
    public void handleClientSide(EntityPlayer player) {
    }

    @Override
    public void handleServerSide(EntityPlayer player) {
        player.world.getTileEntity(pos).getCapability(TilePneumaticTubeBase.CAPABILITY, null).getModule(side).handleClientPacket(data);
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeEnumValue(side);
        buf.writeByteArray(data.array());
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        side = buf.readEnumValue(EnumFacing.class);
        data = new PacketBuffer(Unpooled.copiedBuffer(buf.readByteArray()));
    }

}

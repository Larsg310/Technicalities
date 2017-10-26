package com.technicalitiesmc.util.network;

import com.technicalitiesmc.util.block.TileBase;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

import java.io.IOException;

public class PacketTileUpdate extends LocatedPacket<PacketTileUpdate> {

    private byte[] bytes;

    public PacketTileUpdate(TileBase tile) {
        super(tile);
        try {
            PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
            tile.writeDescription(buf);
            bytes = buf.array();
        } catch (IOException e) {
            e.printStackTrace();
            bytes = new byte[0];
        }
    }

    public PacketTileUpdate() {
    }

    @Override
    public void handleClientSide(EntityPlayer player) {
        TileEntity te = player.world.getTileEntity(pos);
        if (te != null && te instanceof TileBase) {
            try {
                ((TileBase) te).readDescription(new PacketBuffer(Unpooled.copiedBuffer(bytes)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void handleServerSide(EntityPlayer player) {

    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeByteArray(bytes);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        bytes = buf.readByteArray();
    }

}

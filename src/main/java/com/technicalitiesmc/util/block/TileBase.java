package com.technicalitiesmc.util.block;

import com.google.common.base.Throwables;
import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.util.network.PacketTileUpdate;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class TileBase extends TileEntity {

    private static final Set<TileBase> tiles = new HashSet<>();

    public void save() {
        super.markDirty();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        sync();
    }

    public void sync() {
        if (getWorld() != null && getPos() != null && !getWorld().isRemote) {
            tiles.add(this);
        }
    }

    public void writeDescription(PacketBuffer buf) throws IOException {
    }

    public void readDescription(PacketBuffer buf) throws IOException {
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        try {
            writeDescription(buf);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
        tag.setByteArray("data", buf.array());
        return tag;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        try {
            readDescription(new PacketBuffer(Unpooled.copiedBuffer(tag.getByteArray("data"))));
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tag = new NBTTagCompound();
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        try {
            writeDescription(buf);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
        tag.setByteArray("data", buf.array());
        return new SPacketUpdateTileEntity(getPos(), 0, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        try {
            readDescription(new PacketBuffer(Unpooled.copiedBuffer(pkt.getNbtCompound().getByteArray("data"))));
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public static void sendSyncPackets() {
        for (TileBase te : tiles) {
            Technicalities.networkHandler.sendToAllAround(new PacketTileUpdate(te), te.getWorld());
        }
        tiles.clear();
    }

}

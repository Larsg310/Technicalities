package com.technicalitiesmc.util.block;

import java.io.IOException;

import com.google.common.base.Throwables;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileBase extends TileEntity {

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
            // TODO: Add sync packet
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

}

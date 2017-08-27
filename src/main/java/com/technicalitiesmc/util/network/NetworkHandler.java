package com.technicalitiesmc.util.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkHandler {

    public final SimpleNetworkWrapper wrapper;
    private int lastDiscriminator = 0;

    public NetworkHandler(String modid) {
        wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(modid);
    }

    public <P extends Packet<P>> void registerPacket(Class<P> packetType, Side side) {
        wrapper.registerMessage(packetType, packetType, lastDiscriminator++, side);
    }

    public void sendToAll(Packet<?> packet) {
        wrapper.sendToAll(packet);
    }

    public void sendTo(Packet<?> packet, EntityPlayerMP player) {
        wrapper.sendTo(packet, player);
    }

    @SuppressWarnings("rawtypes")
    public void sendToAllAround(LocatedPacket packet, World world, double range) {
        sendToAllAround(packet, packet.getTargetPoint(world, range));
    }

    @SuppressWarnings("rawtypes")
    public void sendToAllAround(LocatedPacket packet, World world) {
        sendToAllAround(packet, packet.getTargetPoint(world, 64));
    }

    public void sendToAllAround(Packet<?> packet, World world, BlockPos pos) {
        sendToAllAround(packet, world, pos, 64);
    }

    public void sendToAllAround(Packet<?> packet, World world, BlockPos pos, double range) {
        sendToAllAround(packet, new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), range));
    }

    public void sendToAllAround(Packet<?> packet, TargetPoint point) {
        wrapper.sendToAllAround(packet, point);
    }

    public void sendToDimension(Packet<?> packet, int dimensionId) {
        wrapper.sendToDimension(packet, dimensionId);
    }

    public void sendToServer(Packet<?> packet) {
        wrapper.sendToServer(packet);
    }

}

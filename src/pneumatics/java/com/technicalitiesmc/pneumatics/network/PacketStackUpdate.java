package com.technicalitiesmc.pneumatics.network;

import java.io.IOException;

import com.technicalitiesmc.pneumatics.tube.TubeStack;
import com.technicalitiesmc.pneumatics.tube.TubeTicker;
import com.technicalitiesmc.util.network.Packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class PacketStackUpdate extends Packet<PacketStackUpdate> {

    private TubeStack stack;

    private long id;
    private ItemStack item;
    private EnumDyeColor color;

    public PacketStackUpdate(TubeStack stack) {
        this.stack = stack;
    }

    public PacketStackUpdate() {
    }

    @Override
    public void handleClientSide(EntityPlayer player) {
        if (!item.isEmpty()) {
            player.world.getCapability(TubeTicker.CAPABILITY, null).update(id, item, color);
        }
    }

    @Override
    public void handleServerSide(EntityPlayer player) {

    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeLong(stack.getID());
        buf.writeItemStack(stack.getStack());
        if (stack.getColor() != null) {
            buf.writeBoolean(true);
            buf.writeEnumValue(stack.getColor());
        } else {
            buf.writeBoolean(false);
        }
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        id = buf.readLong();
        try {
            item = buf.readItemStack();
            color = buf.readBoolean() ? buf.readEnumValue(EnumDyeColor.class) : null;
        } catch (IOException e) {
            item = ItemStack.EMPTY;
        }
    }

}

package com.technicalitiesmc.base.network;

import com.technicalitiesmc.base.Technicalities;
import com.technicalitiesmc.lib.network.LocatedPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class PacketGuiButton extends LocatedPacket<PacketGuiButton> {

    private int button;

    public PacketGuiButton(BlockPos pos, int button) {
        super(pos);
        this.button = button;
    }

    public PacketGuiButton() {
    }

    @Override
    public void handleClientSide(EntityPlayer player) {

    }

    @Override
    public void handleServerSide(EntityPlayer player) {
        player.world.getBlockState(pos).onBlockEventReceived(player.world, pos, -1, button);
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeInt(button);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        button = buf.readInt();
    }

    public static void send(BlockPos pos, int button) {
        Technicalities.networkHandler.sendToServer(new PacketGuiButton(pos, button));
    }

}

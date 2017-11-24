package com.technicalitiesmc.pneumatics.tile;

import com.technicalitiesmc.api.pneumatics.EnumTubeDirection;
import com.technicalitiesmc.api.pneumatics.IPneumaticTube;
import com.technicalitiesmc.api.pneumatics.ITubeStack;
import com.technicalitiesmc.api.pneumatics.TubeModule;
import com.technicalitiesmc.pneumatics.client.TESRPneumaticTube;
import com.technicalitiesmc.util.client.SpecialRenderer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.EnumSet;
import java.util.Set;

@SpecialRenderer(TESRPneumaticTube.class)
public class TilePneumaticTubeClient extends TilePneumaticTubeBase {

    private final Set<EnumFacing> connections = EnumSet.noneOf(EnumFacing.class);

    @Override
    public <T extends TubeModule> boolean setModule(EnumFacing face, TubeModule.Type<T> type) {
        return true;
    }

    @Override
    public IPneumaticTube.INeighbor getNeighbor(EnumFacing side) {
        return null;
    }

    @Override
    public boolean isConnected(EnumFacing side) {
        return connections.contains(side);
    }

    @Override
    public void insertStack(EnumFacing side, float position, EnumTubeDirection direction, ItemStack stack, EnumDyeColor color) {
    }

    @Override
    public void removeStack(ITubeStack stack) {
    }

    @Override
    public void onDataPacket(int id, NBTTagCompound tag) {
        if (id == 2){
            int connections = tag.getInteger("sides");
            for (EnumFacing face : EnumFacing.VALUES) {
                if ((connections & (1 << face.ordinal())) != 0) {
                    this.connections.add(face);
                } else {
                    this.connections.remove(face);
                }
            }
        } else {
            super.onDataPacket(id, tag);
        }
    }

    /*

		@Override
		public void readDescription(PacketBuffer buf) {
			super.readDescription(buf);

			int connections = buf.readInt();
			for (EnumFacing face : EnumFacing.VALUES) {
				if ((connections & (1 << face.ordinal())) != 0) {
					this.connections.add(face);
				} else {
					this.connections.remove(face);
				}
			}
		}
	*/
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return super.getRenderBoundingBox().grow(0.25);
    }

}

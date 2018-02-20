package com.technicalitiesmc.mechanical.tile;

import com.technicalitiesmc.api.mechanical.IKineticNode;
import com.technicalitiesmc.api.mechanical.IShaftAttachable;
import com.technicalitiesmc.api.util.ObjFloatConsumer;
import com.technicalitiesmc.lib.block.TileBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import java.util.function.BiPredicate;

public class TileKineticTest extends TileBase {

    private final IKineticNode top = IKineticNode.create(new TopHost());
    private final IKineticNode bottom = IKineticNode.create(new BottomHost());

    public void debug() {
        System.out.println("--------------------------------------------");
        System.out.println("T > " + top);
        System.out.println("B > " + bottom);
    }

    @Override
    public void validate() {
        top.validate(getWorld().isRemote);
        bottom.validate(getWorld().isRemote);
        super.validate();
    }

    @Override
    public void onLoad() {
        top.validate(getWorld().isRemote);
        bottom.validate(getWorld().isRemote);
        super.onLoad();
    }

    @Override
    public void invalidate() {
        top.invalidate();
        bottom.invalidate();
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        top.invalidate();
        bottom.invalidate();
        super.onChunkUnload();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == IShaftAttachable.CAPABILITY && facing.getAxis() == Axis.Y) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == IShaftAttachable.CAPABILITY && facing.getAxis() == Axis.Y) {
            return (T) (IShaftAttachable) () -> (facing == EnumFacing.UP ? top : bottom);
        }
        return super.getCapability(capability, facing);
    }

    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = getDefaultUpdateTag();
        tag.setTag("top", top.serializeNBT());
        tag.setTag("bottom", bottom.serializeNBT());
        return tag;
    }

    @Override
    public void onDataPacket(int id, NBTTagCompound tag) {
        top.deserializeNBT(tag.getCompoundTag("top"));
        bottom.deserializeNBT(tag.getCompoundTag("bottom"));
    }

    private class TopHost implements IKineticNode.Host {

        @Override
        public World getKineticWorld() {
            return getWorld();
        }

        @Override
        public ChunkPos getKineticChunk() {
            return new ChunkPos(getPos());
        }

        @Override
        public float getAppliedPower() {
            TileEntity te = getWorld().getTileEntity(getPos().up());
            if (te != null && te.hasCapability(IShaftAttachable.CAPABILITY, EnumFacing.DOWN)) {
                return 20;
            }
            return 0;
        }

        @Override
        public float getInertia() {
            return 4;
        }

        @Override
        public void addNeighbors(ObjFloatConsumer<IKineticNode> neighbors, BiPredicate<World, BlockPos> posValidator) {
            IKineticNode.findShaft(getWorld(), getPos(), EnumFacing.UP, 1, neighbors, posValidator);
            IKineticNode.findShaft(getWorld(), getPos(), EnumFacing.NORTH, 1, neighbors, posValidator);
            IKineticNode.findShaft(getWorld(), getPos(), EnumFacing.SOUTH, 1, neighbors, posValidator);
            IKineticNode.findShaft(getWorld(), getPos(), EnumFacing.WEST, 1, neighbors, posValidator);
            IKineticNode.findShaft(getWorld(), getPos(), EnumFacing.EAST, 1, neighbors, posValidator);
        }

    }

    private class BottomHost implements IKineticNode.Host {

        @Override
        public World getKineticWorld() {
            return getWorld();
        }

        @Override
        public ChunkPos getKineticChunk() {
            return new ChunkPos(getPos());
        }

        @Override
        public float getAppliedPower() {
            return 0;
        }

        @Override
        public float getInertia() {
            return 15;
        }

        @Override
        public void addNeighbors(ObjFloatConsumer<IKineticNode> neighbors, BiPredicate<World, BlockPos> posValidator) {
            IKineticNode.findShaft(getWorld(), getPos(), EnumFacing.DOWN, 1, neighbors, posValidator);
        }

    }

}

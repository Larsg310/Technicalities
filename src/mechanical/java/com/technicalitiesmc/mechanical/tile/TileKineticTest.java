package com.technicalitiesmc.mechanical.tile;

import com.technicalitiesmc.api.mechanical.IKineticNode;
import com.technicalitiesmc.api.mechanical.IShaftAttachable;
import com.technicalitiesmc.api.util.ObjFloatConsumer;
import com.technicalitiesmc.lib.block.TileBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

public class TileKineticTest extends TileBase {

    private final IKineticNode generator = IKineticNode.create(new GenHost());
    private final IKineticNode receiver = IKineticNode.create(new RecvHost());

    public void debug() {
        System.out.println("--------------------------------------------");
        System.out.println("T > " + generator);
        System.out.println("B > " + receiver);
    }

    @Override
    public void validate() {
        generator.validate(getWorld().isRemote);
        receiver.validate(getWorld().isRemote);
        super.validate();
    }

    @Override
    public void onLoad() {
        generator.validate(getWorld().isRemote);
        receiver.validate(getWorld().isRemote);
        super.onLoad();
    }

    @Override
    public void invalidate() {
        generator.invalidate();
        receiver.invalidate();
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        generator.invalidate();
        receiver.invalidate();
        super.onChunkUnload();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        if (capability == IShaftAttachable.CAPABILITY) return true;
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        if (capability == IShaftAttachable.CAPABILITY) {
            return (T) (IShaftAttachable) () -> (facing == EnumFacing.DOWN ? receiver : generator);
        }
        return super.getCapability(capability, facing);
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = getDefaultUpdateTag();
        tag.setTag("generator", generator.serializeNBT());
        tag.setTag("receiver", receiver.serializeNBT());
        return tag;
    }

    @Override
    public void onDataPacket(int id, NBTTagCompound tag) {
        generator.deserializeNBT(tag.getCompoundTag("generator"));
        receiver.deserializeNBT(tag.getCompoundTag("receiver"));
    }

    private class GenHost implements IKineticNode.Host {

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
            boolean shouldProducePower = Stream.of(EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST)
                    .map(f -> Pair.of(f, getWorld().getTileEntity(getPos().offset(f))))
                    .filter(f -> f.getRight() != null)
                    .anyMatch(f -> f.getRight().hasCapability(IShaftAttachable.CAPABILITY, f.getLeft().getOpposite()));
            return shouldProducePower ? 20 : 0;
        }

        @Override
        public float getInertia() {
            return 4;
        }

        @Override
        public void addNeighbors(ObjFloatConsumer<IKineticNode> neighbors, BiPredicate<World, BlockPos> posValidator) {
            Stream.of(EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST)
                    .forEach(f -> IKineticNode.findShaft(getWorld(), getPos(), f, 1, neighbors, posValidator));
        }

    }

    private class RecvHost implements IKineticNode.Host {

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

package com.technicalitiesmc.mechanical.tile;

import com.technicalitiesmc.api.mechanical.IGearAttachable;
import com.technicalitiesmc.api.mechanical.IKineticNode;
import com.technicalitiesmc.api.mechanical.IShaftAttachable;
import com.technicalitiesmc.api.util.ObjFloatConsumer;
import com.technicalitiesmc.lib.client.SpecialRenderer;
import com.technicalitiesmc.mechanical.client.TESRRotating;
import net.minecraft.block.BlockDirectional;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import java.util.function.BiPredicate;

@SpecialRenderer(TESRRotating.class)
public class TileGear extends TileRotating implements IKineticNode.Host {

    private final IKineticNode node = IKineticNode.create(this);

    @Override
    public World getKineticWorld() {
        return getWorld();
    }

    @Override
    public ChunkPos getKineticChunk() {
        return new ChunkPos(getPos());
    }

    @Override
    public void validate() {
        node.validate(getWorld().isRemote);
        super.validate();
    }

    @Override
    public void onLoad() {
        node.validate(getWorld().isRemote);
        super.onLoad();
    }

    @Override
    public void invalidate() {
        node.invalidate();
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        node.invalidate();
        super.onChunkUnload();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == IShaftAttachable.CAPABILITY && facing.ordinal() == getBlockMetadata()) {
            return true;
        } else if (capability == IGearAttachable.CAPABILITY && facing.getAxis() != EnumFacing.VALUES[getBlockMetadata()].getAxis()) {
            return true;// TODO: Make pretty
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        EnumFacing face = EnumFacing.VALUES[getBlockMetadata()];
        if (capability == IShaftAttachable.CAPABILITY && facing.ordinal() == getBlockMetadata()) {
            return (T) (IShaftAttachable) () -> node;
        } else if (capability == IGearAttachable.CAPABILITY && facing.getAxis() != face.getAxis()) {
            return (T) (IGearAttachable) f -> f == face ? node : null;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public float getAppliedPower() {
        return 0;
    }

    @Override
    public float getInertia() {
        return 4;
    }

    @Override
    public void addNeighbors(ObjFloatConsumer<IKineticNode> neighbors, BiPredicate<World, BlockPos> posValidator) {
        EnumFacing face = getWorld().getBlockState(getPos()).getValue(BlockDirectional.FACING);
        IKineticNode.findShaft(getWorld(), getPos(), face, 1, neighbors, posValidator);
        for (EnumFacing side : EnumFacing.VALUES) {
            if (side.getAxis() == face.getAxis()) continue;
            IKineticNode.findGear(getWorld(), getPos(), face, side, -1, neighbors, posValidator);
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        tag.setTag("node", node.serializeNBT());
        return tag;
    }

    @Override
    public void onDataPacket(int id, NBTTagCompound tag) {
        node.deserializeNBT(tag.getCompoundTag("node"));
    }

    public float getAngle(float partialTicks) {
        boolean rotateExtra = (getPos().getX() + getPos().getY() + getPos().getZ() + getBlockMetadata()) % 2 == 1;
        return node.getAngle(partialTicks) + (rotateExtra ? 360 / 16F : 0);
    }

    @Override
    public float getScale() {
        return 1.0625F;
    }

    public float getVelocity() {
        return node.getVelocity();
    }

}

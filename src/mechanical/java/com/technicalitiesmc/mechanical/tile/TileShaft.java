package com.technicalitiesmc.mechanical.tile;

import com.technicalitiesmc.api.mechanical.IKineticNode;
import com.technicalitiesmc.api.mechanical.IShaftAttachable;
import com.technicalitiesmc.api.util.ObjFloatConsumer;
import com.technicalitiesmc.lib.client.SpecialRenderer;
import com.technicalitiesmc.mechanical.client.TESRRotating;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import java.util.function.BiPredicate;

@SpecialRenderer(TESRRotating.class)
public class TileShaft extends TileRotating implements IKineticNode.Host {

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

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        if (capability == IShaftAttachable.CAPABILITY && facing.getAxis().ordinal() == getBlockMetadata()) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        if (capability == IShaftAttachable.CAPABILITY && facing.getAxis().ordinal() == getBlockMetadata()) {
            return (T) (IShaftAttachable) () -> node;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public float getAppliedPower() {
        return 0;
    }

    @Override
    public float getInertia() {
        return 1;
    }

    @Nonnull
    @Override
    public EnumFacing.Axis getRotationAxis() {
        return getWorld().getBlockState(getPos()).getValue(BlockRotatedPillar.AXIS);
    }

    @Override
    public void addNeighbors(ObjFloatConsumer<IKineticNode> neighbors, BiPredicate<World, BlockPos> posValidator) {
        EnumFacing.Axis axis = getRotationAxis();
        EnumFacing dir = EnumFacing.getFacingFromAxis(AxisDirection.NEGATIVE, axis);
        IKineticNode.findShaft(getWorld(), getPos(), dir, 1, neighbors, posValidator);
        IKineticNode.findShaft(getWorld(), getPos(), dir.getOpposite(), 1, neighbors, posValidator);
    }

    @SuppressWarnings("deprecation")
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
        return node.getAngle(partialTicks);
    }

}

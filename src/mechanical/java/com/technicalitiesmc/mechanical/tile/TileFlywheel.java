package com.technicalitiesmc.mechanical.tile;

import com.technicalitiesmc.api.mechanical.IKineticNode;
import com.technicalitiesmc.api.mechanical.IShaftAttachable;
import com.technicalitiesmc.api.util.ObjFloatConsumer;
import com.technicalitiesmc.lib.client.SpecialRenderer;
import com.technicalitiesmc.mechanical.client.TESRFlywheel;
import com.technicalitiesmc.mechanical.init.TKMechanicalItems;
import com.technicalitiesmc.mechanical.item.ItemDisk;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import java.util.function.BiPredicate;

@SpecialRenderer(TESRFlywheel.class)
public class TileFlywheel extends TileRotating implements IKineticNode.Host {
    private static final int MAX_DISKS = 5;
    private int diskCount = 0; // cheapo counter since we currently only have 1 type of disk

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
    public float getInertia() {
        return 1.25f + diskCount * 20; // 20: mass of stone disk
    }

    @Override
    public void addNeighbors(ObjFloatConsumer<IKineticNode> neighbors, BiPredicate<World, BlockPos> posValidator) {
        IKineticNode.findShaft(getWorld(), getPos(), EnumFacing.UP, 1, neighbors, posValidator);
        IKineticNode.findShaft(getWorld(), getPos(), EnumFacing.DOWN, 1, neighbors, posValidator);
    }

    public int getDiskCount() {
        return diskCount;
    }

    public void dropDisks() {
        if (world.isRemote) return;
        EntityItem item = new EntityItem(getWorld(), getPos().getX() + 0.5f, getPos().getY() + 0.5f, getPos().getZ() + 0.5f, new ItemStack(TKMechanicalItems.stone_disk, diskCount));
        getWorld().spawnEntity(item);
        diskCount = 0;
    }


    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setByte("dc", (byte) diskCount);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        diskCount = compound.getByte("dc");
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

    public boolean tryAddItem(ItemStack stack) {
        if (diskCount >= MAX_DISKS) return false;
        if (!(stack.item instanceof ItemDisk)) return false;
        if (getWorld().getBlockState(getPos().up()).getBlock() != Blocks.AIR) return false;

        diskCount++;
        return true;
    }

    @Override
    public float getAngle(float partialTicks) {
        return node.getAngle(partialTicks);
    }

    @Override
    public float getScale() {
        return 1.0f;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return getCapability(capability, facing) != null;
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        if (capability == IShaftAttachable.CAPABILITY && facing.getAxis() == EnumFacing.Axis.Y) {
            return (T) (IShaftAttachable) () -> node;
        }
        return super.getCapability(capability, facing);
    }
}

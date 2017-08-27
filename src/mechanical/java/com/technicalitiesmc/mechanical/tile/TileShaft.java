package com.technicalitiesmc.mechanical.tile;

import com.technicalitiesmc.api.mechanical.IKineticNode;
import com.technicalitiesmc.api.mechanical.IShaftAttachable;
import com.technicalitiesmc.api.util.ObjFloatConsumer;
import com.technicalitiesmc.mechanical.kinesis.KineticManager;
import com.technicalitiesmc.mechanical.kinesis.KineticNode;
import com.technicalitiesmc.util.block.TileBase;

import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraftforge.common.capabilities.Capability;

public class TileShaft extends TileBase implements IKineticNode.Host {

    private final IKineticNode node = new KineticNode(this);

    public void debug() {
        System.out.println("--------------------------------------------");
        System.out.println("N > " + node);
    }

    @Override
    public void validate() {
        if (!getWorld().isRemote) {
            KineticManager.INSTANCE.add(node);
        }
        super.validate();
    }

    @Override
    public void onLoad() {
        if (!getWorld().isRemote) {
            KineticManager.INSTANCE.add(node);
        }
        super.onLoad();
    }

    @Override
    public void invalidate() {
        if (!getWorld().isRemote) {
            KineticManager.INSTANCE.remove(node);
        }
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        if (!getWorld().isRemote) {
            KineticManager.INSTANCE.remove(node);
        }
        super.onChunkUnload();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == IShaftAttachable.CAPABILITY && facing.getAxis().ordinal() == getBlockMetadata()) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == IShaftAttachable.CAPABILITY && facing.getAxis().ordinal() == getBlockMetadata()) {
            return (T) (IShaftAttachable) b -> node;
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

    @Override
    public void addNeighbors(ObjFloatConsumer<IKineticNode> neighbors) {
        EnumFacing.Axis axis = getWorld().getBlockState(getPos()).getValue(BlockRotatedPillar.AXIS);
        EnumFacing dir1 = EnumFacing.getFacingFromAxis(AxisDirection.NEGATIVE, axis);
        EnumFacing dir2 = EnumFacing.getFacingFromAxis(AxisDirection.POSITIVE, axis);

        TileEntity te = getWorld().getTileEntity(getPos().offset(dir1));
        if (te != null && te.hasCapability(IShaftAttachable.CAPABILITY, dir2)) {
            neighbors.accept(te.getCapability(IShaftAttachable.CAPABILITY, dir2).getNode(false), 1);
        }

        te = getWorld().getTileEntity(getPos().offset(dir2));
        if (te != null && te.hasCapability(IShaftAttachable.CAPABILITY, dir1)) {
            neighbors.accept(te.getCapability(IShaftAttachable.CAPABILITY, dir1).getNode(false), 1);
        }
    }

}

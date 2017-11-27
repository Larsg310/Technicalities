package com.technicalitiesmc.mechanical.tile;

import com.technicalitiesmc.api.mechanical.IKineticNode;
import com.technicalitiesmc.api.mechanical.IShaftAttachable;
import com.technicalitiesmc.api.util.ObjFloatConsumer;
import com.technicalitiesmc.lib.block.TileBase;
import com.technicalitiesmc.mechanical.kinesis.KineticManager;
import com.technicalitiesmc.mechanical.kinesis.KineticNode;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraftforge.common.capabilities.Capability;

public class TileKineticTest extends TileBase {

    private final IKineticNode top = new KineticNode(new TopHost());
    private final IKineticNode bottom = new KineticNode(new BottomHost());

    public void debug() {
        System.out.println("--------------------------------------------");
        System.out.println("T > " + top);
        System.out.println("B > " + bottom);
    }

    @Override
    public void validate() {
        if (!getWorld().isRemote) {
            KineticManager.INSTANCE.add(top);
            KineticManager.INSTANCE.add(bottom);
        }
        super.validate();
    }

    @Override
    public void onLoad() {
        if (!getWorld().isRemote) {
            KineticManager.INSTANCE.add(top);
            KineticManager.INSTANCE.add(bottom);
        }
        super.onLoad();
    }

    @Override
    public void invalidate() {
        if (!getWorld().isRemote) {
            KineticManager.INSTANCE.remove(top);
            KineticManager.INSTANCE.remove(bottom);
        }
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        if (!getWorld().isRemote) {
            KineticManager.INSTANCE.remove(top);
            KineticManager.INSTANCE.remove(bottom);
        }
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
            return (T) (IShaftAttachable) b -> (facing == EnumFacing.UP ? top : bottom);
        }
        return super.getCapability(capability, facing);
    }

    private class TopHost implements IKineticNode.Host {

        @Override
        public float getAppliedPower() {
            TileEntity te = getWorld().getTileEntity(getPos().up());
            if (te != null && te.hasCapability(IShaftAttachable.CAPABILITY, EnumFacing.DOWN)) {
                return 5;
            }
            return 0;
        }

        @Override
        public float getInertia() {
            return 1;
        }

        @Override
        public void addNeighbors(ObjFloatConsumer<IKineticNode> neighbors) {
            TileEntity te = getWorld().getTileEntity(getPos().up());
            if (te != null && te.hasCapability(IShaftAttachable.CAPABILITY, EnumFacing.DOWN)) {
                neighbors.accept(te.getCapability(IShaftAttachable.CAPABILITY, EnumFacing.DOWN).getNode(false), 1);
            }
        }

    }

    private class BottomHost implements IKineticNode.Host {

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
            TileEntity te = getWorld().getTileEntity(getPos().down());
            if (te != null && te.hasCapability(IShaftAttachable.CAPABILITY, EnumFacing.UP)) {
                neighbors.accept(te.getCapability(IShaftAttachable.CAPABILITY, EnumFacing.UP).getNode(false), 1);
            }
        }

    }

}

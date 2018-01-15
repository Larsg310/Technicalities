package com.technicalitiesmc.mechanical.tile;

import com.technicalitiesmc.api.mechanical.conveyor.IConveyorBelt;
import com.technicalitiesmc.api.mechanical.conveyor.IConveyorObject;
import com.technicalitiesmc.lib.block.TileBase;
import com.technicalitiesmc.lib.client.SpecialRenderer;
import com.technicalitiesmc.mechanical.block.BlockConveyorSmall;
import com.technicalitiesmc.mechanical.client.TESRConveyor;
import com.technicalitiesmc.mechanical.conveyor.ConveyorBeltLogic;
import com.technicalitiesmc.mechanical.conveyor.IConveyorBeltHost;
import com.technicalitiesmc.mechanical.conveyor.object.ConveyorStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.UUID;

@SpecialRenderer(TESRConveyor.class)
public class TileConveyorSmall extends TileBase implements ITickable, IConveyorBeltHost {
    private static final int PACKET_OBJ_ADD = 10;
    private static final int PACKET_OBJ_REMOVE = 11;
    private static final int PACKET_OBJ_SYNC = 12;

    private final ConveyorBeltLogic logic = new ConveyorBeltLogic(this, 9 / 16F);

    @Override
    public void update() {
        logic.tick();
    }

    @Override
    public IConveyorBelt getNeighbor(EnumFacing side) {
        TileEntity te = getWorld().getTileEntity(getPos().offset(side));
        if (te != null && te.hasCapability(IConveyorBelt.CAPABILITY, null)) {
            return te.getCapability(IConveyorBelt.CAPABILITY, null);
        }
        return null;
    }

    @Nonnull
    @Override
    public EnumFacing.Axis getMovementAxis() {
        return getWorld().getBlockState(getPos()).getValue(BlockConveyorSmall.PROPERTY_ROTATION) == 1 ? EnumFacing.Axis.X : EnumFacing.Axis.Z;
    }

    @Override
    public float getMovementSpeed() {
        return 0.01f;
    }

    @Override
    public void notifyObjectAdd(UUID id) {
        if (getWorld().isRemote) return;
        System.out.println("notify add");
        Pair<IConveyorObject, ConveyorBeltLogic.Path> o = logic.getObjects().get(id);
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagCompound data = new NBTTagCompound();
        o.getLeft().saveData(data);
        nbt.setFloat("A", o.getRight().locationOnBelt);
        nbt.setFloat("B", o.getRight().offsetX);
        nbt.setTag("C", data);
        sendPacket(PACKET_OBJ_ADD, nbt);
    }

    @Override
    public void notifyObjectRemove(UUID id) {
        if (getWorld().isRemote) return;
        System.out.println("notify remove");
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setUniqueId("C", id);
        sendPacket(PACKET_OBJ_REMOVE, nbt);
    }

    @Override
    public void onDataPacket(int id, NBTTagCompound tag) {
        switch (id) {
            case PACKET_OBJ_ADD:
                ConveyorBeltLogic.Path p = new ConveyorBeltLogic.Path();
                IConveyorObject co = new ConveyorStack(); // TODO: make this work for non-stacks!!!
                p.locationOnBelt = tag.getFloat("A");
                p.offsetX = tag.getFloat("B");
                co.loadData(tag.getCompoundTag("C"));
                logic.getObjects().put(co.uuid(), Pair.of(co, p));
                break;
            case PACKET_OBJ_REMOVE:
                logic.getObjects().remove(tag.getUniqueId("C"));
                break;
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == IConveyorBelt.CAPABILITY || super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == IConveyorBelt.CAPABILITY) {
            return (T) logic;
        }
        return super.getCapability(capability, facing);
    }
}

package com.technicalitiesmc.mechanical.tile;

import com.technicalitiesmc.api.mechanical.conveyor.IConveyorBelt;
import com.technicalitiesmc.api.mechanical.conveyor.IConveyorObject;
import com.technicalitiesmc.lib.block.TileBase;
import com.technicalitiesmc.mechanical.block.BlockConveyorBase;
import com.technicalitiesmc.mechanical.conveyor.ConveyorBeltLogic;
import com.technicalitiesmc.mechanical.conveyor.IConveyorBeltHost;
import com.technicalitiesmc.mechanical.conveyor.object.ConveyorStack;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class TileConveyorBase extends TileBase implements ITickable, IConveyorBeltHost {
    public static final int PACKET_OBJ_ADD = 10;
    public static final int PACKET_OBJ_REMOVE = 11;

    private final ConveyorBeltLogic logic;

    public boolean b = false; // TODO temp thingy to allow for reverse movement!

    public TileConveyorBase(float height) {
        logic = new ConveyorBeltLogic(this, height);
    }

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
        return getWorld().getBlockState(getPos()).getValue(BlockConveyorBase.PROPERTY_ROTATION) == 1 ? EnumFacing.Axis.X : EnumFacing.Axis.Z;
    }

    @Override
    public float getMovementSpeed() {
        return 0.01f * (b ? -1 : 1);
    }

    @Override
    public void spawnItem(ItemStack stack, Vec3d offset) {
        World world = getWorld();
        BlockPos pos = getPos();

        if (world.isRemote) return;

        EntityItem entity = new EntityItem(
            world,
            pos.getX() + 0.5 + offset.x,
            pos.getY() + logic.getHeight() + offset.y,
            pos.getZ() + 0.5 + offset.z,
            stack
        );

        entity.setVelocity(0, 0, 0);
        world.spawnEntity(entity);
    }

    @Override
    public void pickupEntities(Predicate<Entity> op) {
        if (!world.isRemote) {
            AxisAlignedBB pickupBounds = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.125, 1.0).offset(getPos()).offset(0.0, logic.getHeight(), 0.0);
            world.getEntitiesWithinAABBExcludingEntity(null, pickupBounds)
                .forEach(e -> { if (op.test(e)) e.setDead(); });
        }
    }

    @Nonnull
    @Override
    public Collection<AxisAlignedBB> getObjectBoundingBoxes(int radius, int height, Predicate<IConveyorObject> filter) {
        return getBoundingBoxesFor(radius, height, pos -> {
            TileEntity te = getWorld().getTileEntity(pos);
            if (te != null && te.hasCapability(IConveyorBelt.CAPABILITY, null)) {
                IConveyorBelt cap = te.getCapability(IConveyorBelt.CAPABILITY, null);
                return cap.getAllBoundingBoxes(filter);
            }
            return Collections.emptySet();
        });
    }

    @Nonnull
    @Override
    public Collection<AxisAlignedBB> getWorldBoundingBoxes(int radius, int height) {
        return getBoundingBoxesFor(radius, height, pos -> {
            List<AxisAlignedBB> boxes = new ArrayList<>();
            getWorld().getBlockState(pos).addCollisionBoxToList(getWorld(), pos, Block.FULL_BLOCK_AABB.offset(pos), boxes, null, false);
            return boxes.stream().map(it -> it.offset(BlockPos.ORIGIN.subtract(pos)).offset(-0.5, -logic.getHeight() - 0.25, -0.5)).collect(Collectors.toSet());
        });
    }

    private Collection<AxisAlignedBB> getBoundingBoxesFor(int radius, int height, Function<BlockPos, Collection<AxisAlignedBB>> op) {
        BlockPos pos = getPos();

        Collection<AxisAlignedBB> bbs = new HashSet<>();

        for (int y = -1; y < height; y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos bp = pos.add(x, y, z);
                    final int finalX = x, finalY = y, finalZ = z; // f u java ಠ_ಠ
                    op.apply(bp).stream().map(it -> it.offset(finalX, finalY, finalZ)).forEach(bbs::add);
                }
            }
        }

        return bbs;
    }

    @Override
    public void notifyObjectAdd(UUID id) {
        if (getWorld().isRemote) return;
        System.out.println("notify add");
        Pair<IConveyorObject, ConveyorBeltLogic.Path> o = logic.getObjects().get(id);
        NBTTagCompound nbt = logic.createData(o);
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
                IConveyorObject co = new ConveyorStack(); // TODO: make this work for non-stacks!!!
                NBTTagCompound objData = tag.getCompoundTag("object");
                NBTTagCompound pathData = tag.getCompoundTag("path");
                ConveyorBeltLogic.Path path = ConveyorBeltLogic.Path.createFromNBT(pathData);
                co.loadData(objData);
                logic.getObjects().put(co.uuid(), Pair.of(co, path));
                break;
            case PACKET_OBJ_REMOVE:
                logic.getObjects().remove(tag.getUniqueId("C"));
                break;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        b = nbt.getBoolean("invert");
        logic.loadData(nbt.getCompoundTag("logic"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setBoolean("invert", b);

        NBTTagCompound local = new NBTTagCompound();
        logic.saveData(local);
        nbt.setTag("logic", local);

        return nbt;
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

package com.technicalitiesmc.mechanical.conveyor;

import com.google.common.collect.Lists;
import com.technicalitiesmc.api.mechanical.conveyor.IConveyorBelt;
import com.technicalitiesmc.api.mechanical.conveyor.IConveyorObject;
import com.technicalitiesmc.mechanical.conveyor.object.ConveyorStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ConveyorBeltLogic implements IConveyorBelt {
    private final IConveyorBeltHost host;
    private final float height;

    private final Map<UUID, Pair<IConveyorObject, Path>> objects = new HashMap<>();

    public ConveyorBeltLogic(IConveyorBeltHost host, float height) {
        this.host = host;
        this.height = height;
    }

    public void tick() {
        World world = host.getWorld();
        BlockPos pos = host.getPos();
        EnumFacing.Axis axis = host.getMovementAxis();

        if (axis == EnumFacing.Axis.Y) return; // wtf?

        for (Pair<IConveyorObject, Path> o : Lists.newArrayList(objects.values())) {
            IConveyorObject co = o.getLeft();
            Path p = o.getRight();
            UUID uuid = co.uuid();

            p.saveLastLocation();

            // nudge items into the center
            float x = p.offsetX * 2;
            float x3 = Math.max(-1, Math.min(x * x * x * host.getMovementSpeed() * 10f, 1));
            p.offsetX -= x3;

            p.locationOnBelt += host.getMovementSpeed();
            // if this item is about to fall off the belt, transfer it to the next one
            if (host.getMovementSpeed() * p.locationOnBelt > 0 && Math.abs(p.locationOnBelt) > 1) {
                EnumFacing f = host.getEjectFacing();
                IConveyorBelt next = host.getNeighbor(f);
                if (next != null && next.canInput(f.getOpposite())) {
                    if (next instanceof ConveyorBeltLogic) {
                        p.locationOnBelt -= Math.signum(host.getMovementSpeed());
                        if (next.getOrientation() != this.getOrientation()) Path.rotatePath(p);
                        p.saveLastLocation();
                        ((ConveyorBeltLogic) next).insert(f.getOpposite(), co, p);
                    } else {
                        next.insert(f.getOpposite(), co);
                    }
                    objects.remove(uuid);
                    host.notifyObjectRemove(uuid);
                }
            }

        }

        if (!world.isRemote) {
            world.getEntitiesWithinAABBExcludingEntity(null, getPickupBounds()).stream()
                    .map(it -> Pair.of(it, fromEntity(it)))
                    .filter(it -> it.getRight().isPresent())
                    .map(it -> Pair.of(it.getLeft(), it.getRight().get()))
                    .forEach(it -> {
                        Entity e = it.getLeft();
                        IConveyorObject co = it.getRight();

                        Vec3d relativePos = e.getPositionVector().subtract(pos.getX(), pos.getY() + height, pos.getZ());
                        float rx = (float) relativePos.x;
                        float rz = (float) relativePos.z;
                        if (rx >= 0 && rx <= 1 && rz >= 0 && rz <= 1) {
                            float offsetX = (axis == EnumFacing.Axis.X ? rz : rx) - 0.5f;
                            float locationOnBelt = axis == EnumFacing.Axis.X ? rx : rz;
                            Path path = new Path();
                            path.offsetX = offsetX;
                            path.locationOnBelt = locationOnBelt;
                            objects.put(co.uuid(), Pair.of(co, path));
                            e.setDead();
                            host.notifyObjectAdd(co.uuid());
                        }
                    });
        }
    }

    private AxisAlignedBB getPickupBounds() {
        return new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.125, 1.0).offset(host.getPos()).offset(0.0, height, 0.0);
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public boolean canInput(EnumFacing side) {
        return side.getAxis().isHorizontal();
    }

    @Override
    public void insert(EnumFacing side, IConveyorObject object) {
        insert(side, object, null);
    }

    public void insert(EnumFacing side, IConveyorObject object, @Nullable Path path) {
        if (path == null) path = new Path(); // TODO: implement side-specific behavior as fallback
        objects.put(object.uuid(), Pair.of(object, path));
        host.notifyObjectAdd(object.uuid());
    }

    public Map<UUID, Pair<IConveyorObject, Path>> getObjects() {
        return objects;
    }

    @Nonnull
    @Override
    public EnumFacing.Axis getOrientation() {
        return host.getMovementAxis();
    }

    public void saveData(NBTTagCompound nbt) {}

    public void loadData(NBTTagCompound nbt) {}

    private Optional<IConveyorObject> fromEntity(Entity e) {
        IConveyorObject o = null;
        if (e instanceof EntityItem) o = new ConveyorStack(((EntityItem) e).getItem().copy());
        return Optional.ofNullable(o);
    }

    public static class Path {
        // offset from center towards sides (-0.5 - 0.5)
        public float offsetX = 0.0f;

        // movement progress (0.0 - 1.0)
        public float locationOnBelt = 0.5f;

        public float lastOffsetX = 0.0f;
        public float lastLocationOnBelt = 0.5f;

        public void saveLastLocation() {
            lastOffsetX = offsetX;
            lastLocationOnBelt = locationOnBelt;
        }

        private static float interp(float a, float b, float partialTicks) {
            return a + (b - a) * partialTicks;
        }

        public static Vec3d getOffset(Path self, IConveyorBeltHost host, float partialTicks) {
            float offsetFixed = interp(self.lastOffsetX, self.offsetX, partialTicks) + 0.5f;
            float lobFixed = interp(self.lastLocationOnBelt, self.locationOnBelt, partialTicks);
            float x = host.getMovementAxis() == EnumFacing.Axis.X ? lobFixed : offsetFixed;
            float z = host.getMovementAxis() == EnumFacing.Axis.X ? offsetFixed : lobFixed;
            return new Vec3d(x, 0, z);
        }

        public static void rotatePath(Path self) {
            float t = self.offsetX + 0.5f;
            float u = self.locationOnBelt - 0.5f;
            self.locationOnBelt = t;
            self.offsetX = u;
        }
    }
}

package com.technicalitiesmc.mechanical.conveyor;

import com.google.common.collect.Lists;
import com.technicalitiesmc.api.mechanical.conveyor.IConveyorBelt;
import com.technicalitiesmc.api.mechanical.conveyor.IConveyorObject;
import com.technicalitiesmc.mechanical.conveyor.object.ConveyorStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ConveyorBeltLogic implements IConveyorBelt {
    private final IConveyorBeltHost host;
    private final float height;

    private final Map<UUID, Pair<IConveyorObject, IPath>> objects = new HashMap<>();

    public ConveyorBeltLogic(IConveyorBeltHost host, float height) {
        this.host = host;
        this.height = height;
    }

    public void tick() {
        BlockPos pos = host.getPos();
        EnumFacing.Axis axis = host.getMovementAxis();

        if (axis == EnumFacing.Axis.Y) return; // wtf?

        Collection<AxisAlignedBB> world = host.getWorldBoundingBoxes(5, 2);

        for (Pair<IConveyorObject, IPath> o : Lists.newArrayList(objects.values())) {
            IConveyorObject co = o.getLeft();
            IPath p = o.getRight();
            UUID uuid = co.uuid();
            Collection<AxisAlignedBB> boundingBoxes = host.getObjectBoundingBoxes(5, 2, it -> it != co);
            boundingBoxes.addAll(world);

            if (!p.canTransferToNext(host.getMovementSpeed()))
                p.move(co, getOrientation(), boundingBoxes, host.getMovementSpeed());
            if (p.canTransferToNext(host.getMovementSpeed())) {
                EnumFacing f = host.getEjectFacing();
                IConveyorBelt next = host.getNeighbor(f);
                if (next != null) {
                    if (next.canInput(f.getOpposite())) {
                        next.insert(f.getOpposite(), co);
                        objects.remove(uuid);
                        host.notifyObjectRemove(uuid);
                    }
                } else {
                    host.spawnItem(co.getDropItem(), getDirectionVec(getOrientation()).scale(host.getMovementSpeed()).normalize().scale(0.75));
                    objects.remove(uuid);
                    host.notifyObjectRemove(uuid);
                }
            }
        }

        host.pickupEntities(e -> {
            Optional<IConveyorObject> coOpt = fromEntity(e);
            if (coOpt.isPresent()) {
                IConveyorObject co = coOpt.get();
                Vec3d relativePos = e.getPositionVector().subtract(pos.getX(), pos.getY() + height, pos.getZ());
                float rx = (float) relativePos.x;
                float rz = (float) relativePos.z;
                if (rx >= 0 && rx <= 1 && rz >= 0 && rz <= 1) {
                    IPath path = new PathStraight(0.5f);
                    objects.put(co.uuid(), Pair.of(co, path));
                    host.notifyObjectAdd(co.uuid());
                    return true;
                }
            }
            return false;
        });
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
        IPath path;
        if (side.getAxis() == getOrientation()) {
            path = new PathStraight(side.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? 1 : 0);
        } else {
            path = new PathCurved(side.getAxisDirection().getOffset() > 0 ^ getOrientation() == EnumFacing.Axis.X);
        }
        objects.put(object.uuid(), Pair.of(object, path));
        host.notifyObjectAdd(object.uuid());
    }

    @Override
    public Collection<AxisAlignedBB> getAllBoundingBoxes(Predicate<IConveyorObject> op) {
        return objects.values().stream()
            .flatMap(it -> it.getLeft().bounds().stream().map(bb -> bb.offset(getOffsetVector(it.getRight()))))
            .collect(Collectors.toSet());
    }

    private Vec3d getOffsetVector(IPath path) {
        return getDirectionVec(getOrientation()).scale(path.getProgress() - 0.5);
    }

    private static Vec3d getDirectionVec(EnumFacing.Axis axis) {
        return new Vec3d(EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, axis).getDirectionVec());
    }

    public Map<UUID, Pair<IConveyorObject, IPath>> getObjects() {
        return objects;
    }

    @Nonnull
    @Override
    public EnumFacing.Axis getOrientation() {
        return host.getMovementAxis();
    }

    public NBTTagCompound createData(Pair<IConveyorObject, ConveyorBeltLogic.IPath> pair) {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagCompound pathData = new NBTTagCompound();
        NBTTagCompound objData = new NBTTagCompound();
        pair.getLeft().saveData(objData);
        pair.getRight().saveData(pathData);
        nbt.setTag("object", objData);
        nbt.setTag("path", pathData);
        return nbt;
    }

    @Override
    public void onHostDestroyed() {
        objects.forEach((key, value) -> {
            host.spawnItem(value.getLeft().getDropItem(), new Vec3d(0, 0, 0));
            host.notifyObjectRemove(key);
        });
        objects.clear();
    }

    public void saveData(NBTTagCompound nbt) {
        NBTTagList list = new NBTTagList();
        objects.entrySet().stream()
            .map(it -> {
                NBTTagCompound local = new NBTTagCompound();
                local.setUniqueId("id", it.getKey());
                local.setTag("data", createData(it.getValue()));
                return local;
            })
            .forEach(list::appendTag);
        nbt.setTag("items", list);
    }

    public void loadData(NBTTagCompound nbt) {
        objects.clear();
        NBTTagList list = nbt.getTagList("items", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound local = list.getCompoundTagAt(i);
            UUID uuid = local.getUniqueId("id");
            NBTTagCompound data = local.getCompoundTag("data");
            IConveyorObject co = new ConveyorStack(); // TODO: make this work for non-stacks!!!
            co.loadData(data.getCompoundTag("object"));
            IPath path = IPath.createFromNBT(data.getCompoundTag("path"));
            objects.put(uuid, Pair.of(co, path));
        }
    }

    private Optional<IConveyorObject> fromEntity(Entity e) {
        IConveyorObject o = null;
        if (e instanceof EntityItem) o = new ConveyorStack(((EntityItem) e).getItem().copy());
        return Optional.ofNullable(o);
    }

    private static float interp(float a, float b, float partialTicks) { return a + (b - a) * partialTicks; }

    private static boolean canMoveDistance(AxisAlignedBB box, float dist, EnumFacing.Axis axis, Collection<AxisAlignedBB> obstacles) {
        Vec3d d = getDirectionVec(axis).scale(dist);
        AxisAlignedBB expanded = box.expand(d.x, d.y, d.z);
        return obstacles.stream().allMatch(c -> (box.intersects(c) && expanded.getCenter().squareDistanceTo(c.getCenter()) > box.getCenter().squareDistanceTo(c.getCenter())) || !expanded.intersects(c));
    }

    public static interface IPath {
        public static final byte TYPE_STRAIGHT = 0;
        public static final byte TYPE_CURVED = 1;

        @SideOnly(Side.CLIENT)
        @Nonnull
        public Matrix4f transform(float partialTicks);

        public boolean move(IConveyorObject obj, EnumFacing.Axis axis, Collection<AxisAlignedBB> world, float speed);

        public boolean canTransferToNext(float speed);

        public float getProgress();

        public byte type();

        public void loadData(NBTTagCompound nbt);

        public void saveData(NBTTagCompound nbt);

        public static IPath createFromNBT(NBTTagCompound nbt) {
            int id = nbt.getByte("id");
            IPath path;
            switch (id) {
                case TYPE_STRAIGHT:
                    path = new PathStraight();
                    break;
                case TYPE_CURVED:
                    path = new PathCurved();
                    break;
                default:
                    return new PathStraight();
            }
            path.loadData(nbt);
            System.out.println("Path " + path.getClass());
            return path;
        }
    }

    private static class PathStraight implements IPath {
        private float prevProgress;
        private float progress;

        public PathStraight() {
            this(0.5f);
        }

        public PathStraight(float progress) {
            this.prevProgress = progress;
            this.progress = progress;
        }

        @Nonnull
        @Override
        public Matrix4f transform(float partialTicks) {
            return new Matrix4f()
                .translate(new Vector3f(0f, 0f, interp(prevProgress, progress, partialTicks) - 0.5f));
        }

        @Override
        public boolean move(IConveyorObject obj, EnumFacing.Axis axis, Collection<AxisAlignedBB> world, float speed) {
            prevProgress = progress;

            boolean check = obj.bounds().stream().allMatch(c -> canMoveDistance(c.offset(getDirectionVec(axis).scale(progress - 0.5)), speed, axis, world));

            if (check) progress += speed;
            return check;
        }

        @Override
        public float getProgress() {
            return progress;
        }

        @Override
        public boolean canTransferToNext(float speed) {
            float p = (this.progress - 0.5f) * 2f;
            return speed * p > 0 && Math.abs(p) > 1;
        }

        @Override
        public void loadData(NBTTagCompound nbt) {
            prevProgress = nbt.getFloat("a");
            progress = nbt.getFloat("b");
        }

        @Override
        public void saveData(NBTTagCompound nbt) {
            nbt.setByte("id", type());
            nbt.setFloat("a", prevProgress);
            nbt.setFloat("b", progress);
        }

        @Override
        public byte type() {
            return TYPE_STRAIGHT;
        }
    }

    private static class PathCurved extends PathStraight {
        private float prevOffsetX = 0.0f;
        private float offsetX = 0.0f;
        private float prevRotation = 0f;
        private float rotation = 0f;

        public PathCurved() {
            this(false);
        }

        public PathCurved(boolean right) {
            super(0.5f);
            if (right) {
                prevOffsetX = offsetX = 0.5f;
                rotation = -90.0f;
            } else {
                prevOffsetX = offsetX = -0.5f;
                rotation = 90.0f;
            }
        }

        @Override
        public boolean move(IConveyorObject obj, EnumFacing.Axis axis, Collection<AxisAlignedBB> world, float speed) {
            prevOffsetX = offsetX;
            prevRotation = rotation;
            if (super.move(obj, axis, world, speed)) {
                offsetX = converge(offsetX, speed);
                rotation = converge(rotation, 90f * speed / 0.5f);
                return true;
            }
            return false;
        }

        private float converge(float value, float speed) {
            float sgn = Math.signum(value);
            float abs = Math.abs(value);
            float sabs = Math.abs(speed);
            return Math.max(0, abs - sabs) * sgn;
        }

        @Nonnull
        @Override
        public Matrix4f transform(float partialTicks) {
            return super.transform(partialTicks)
                .translate(new Vector3f(interp(prevOffsetX, offsetX, partialTicks), 0f, 0f))
                .rotate((float) Math.toRadians(interp(prevRotation, rotation, partialTicks)), new Vector3f(0f, 1f, 0f));
        }

        @Override
        public void loadData(NBTTagCompound nbt) {
            super.loadData(nbt);
            prevOffsetX = nbt.getFloat("c");
            offsetX = nbt.getFloat("d");
            prevRotation = nbt.getFloat("e");
            rotation = nbt.getFloat("f");
        }

        @Override
        public void saveData(NBTTagCompound nbt) {
            super.saveData(nbt);
            nbt.setFloat("c", prevOffsetX);
            nbt.setFloat("d", offsetX);
            nbt.setFloat("e", prevRotation);
            nbt.setFloat("f", rotation);
        }

        @Override
        public byte type() {
            return TYPE_CURVED;
        }
    }
}

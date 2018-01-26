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
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.Math.*;

public class ConveyorBeltLogic implements IConveyorBelt {
    private final IConveyorBeltHost host;
    private final float height;

    private final Map<UUID, Pair<IConveyorObject, Path>> objects = new HashMap<>();

    public ConveyorBeltLogic(IConveyorBeltHost host, float height) {
        this.host = host;
        this.height = height;
    }

    public void tick() {
        BlockPos pos = host.getPos();
        EnumFacing.Axis axis = host.getMovementAxis();

        if (axis == EnumFacing.Axis.Y) return; // wtf?

        Collection<AxisAlignedBB> world = host.getWorldBoundingBoxes(2, 2);

        for (Pair<IConveyorObject, Path> o : Lists.newArrayList(objects.values())) {
            IConveyorObject co = o.getLeft();
            Path p = o.getRight();
            UUID uuid = co.uuid();
            Collection<AxisAlignedBB> boundingBoxes = host.getObjectBoundingBoxes(5, 2, it -> it != co);
            boundingBoxes.addAll(world);

            if (!p.canTransferToNext(host.getMovementSpeed())) {
                float rotationSpeed = 0f;

                AxisAlignedBB union = co.bounds().stream()
                    .reduce(null, (i1, i2) -> i1 == null ? i2 : i1.union(i2))
                    .offset(convertToWorldCoords(p.getXOffset(), p.getProgress()));

                for (EnumFacing f : EnumFacing.HORIZONTALS) {
                    AxisAlignedBB aabb = new AxisAlignedBB(BlockPos.ORIGIN.offset(f));
                    if (union.intersects(aabb.minX, Double.NEGATIVE_INFINITY, aabb.minZ, aabb.maxX, Double.POSITIVE_INFINITY, aabb.maxZ)) {
                        IConveyorBelt neighbor = host.getNeighbor(f);
                        if (neighbor != null) {
                            Vec3d v1 = neighbor.getMovementVector().add(getMovementVector()).normalize();
                            Vec3d v2 = v1;
                            if (getOrientation() == EnumFacing.Axis.Z) v2 = new Vec3d(v1.z, v1.y, v1.x);
                            float s = (float) (0.25 * toDegrees(atan2(v2.z, v2.x)));
                            if (s != 0) System.out.println(s);
                            rotationSpeed += s;
                        }
                    }
                }
                p.move(co, getOrientation(), boundingBoxes, host.getMovementSpeed(), rotationSpeed);
            }

            if (p.canTransferToNext(host.getMovementSpeed())) {
                EnumFacing f = host.getEjectFacing();
                IConveyorBelt next = host.getNeighbor(f);
                if (next != null) {
                    if (next.canInput(f.getOpposite())) {
                        if (next instanceof ConveyorBeltLogic) {
                            ((ConveyorBeltLogic) next).insert(f.getOpposite(), co, p.getRotation());
                        } else {
                            next.insert(f.getOpposite(), co);
                        }
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
                    Path path = new Path();
                    objects.put(co.uuid(), Pair.of(co, path));
                    host.notifyObjectAdd(co.uuid());
                    return true;
                }
            }
            return false;
        });
    }

    public Vec3d convertToWorldCoords(float xOffset, float progress) {
        float xo1 = xOffset + 0.5f;
        float x;
        float z;
        if (getOrientation() == EnumFacing.Axis.X) {
            x = progress;
            z = -xo1 + 1;
        } else {
            x = xo1;
            z = progress;
        }
        return new Vec3d(x, 0, z);
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
        insert(side, object, 0f);
    }

    public void insert(EnumFacing side, IConveyorObject object, float rotation) {
        Path path = new Path();
        EnumFacing.Axis inputAxis = side.getAxis();
        if (inputAxis == getOrientation()) {
            path.progress = path.prevProgress = side.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? 1f : 0f;
            path.rotation = path.prevRotation = rotation;
        } else {
            path.xOffset = path.prevXOffset =
                (side.getAxisDirection().getOffset() > 0 ^ getOrientation() == EnumFacing.Axis.X) ? 0.5f : -0.5f;
            path.rotation = path.prevRotation = rotation + (inputAxis == EnumFacing.Axis.X ? 90f : -90f);
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

    private Vec3d getOffsetVector(Path path) {
        return getDirectionVec(getOrientation()).scale(path.getProgress() - 0.5);
    }

    private static Vec3d getDirectionVec(EnumFacing.Axis axis) {
        return new Vec3d(EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, axis).getDirectionVec());
    }

    public Map<UUID, Pair<IConveyorObject, Path>> getObjects() {
        return objects;
    }

    @Nonnull
    @Override
    public EnumFacing.Axis getOrientation() {
        return host.getMovementAxis();
    }

    @Nonnull
    @Override
    public Vec3d getMovementVector() {
        return getDirectionVec(getOrientation()).scale(host.getMovementSpeed());
    }

    public NBTTagCompound createData(Pair<IConveyorObject, Path> pair) {
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
            Path path = Path.createFromNBT(data.getCompoundTag("path"));
            objects.put(uuid, Pair.of(co, path));
        }
    }

    private Optional<IConveyorObject> fromEntity(Entity e) {
        // TODO: registry that creates IConveyorObject from Entity
        IConveyorObject o = null;
        if (e instanceof EntityItem) o = new ConveyorStack(((EntityItem) e).getItem().copy());
        return Optional.ofNullable(o);
    }

    // Interpolates (linearly) two values
    private static float interp(float a, float b, float partialTicks) { return a + (b - a) * partialTicks; }

    // // Returns the closer of the two possible directions to the target
    // private static float flow(float src, float speed, float target, float min, float max) {
    //     float rspeed = min(speed, wcmp(src, target, min, max));
    //     if (rspeed == 0f) return src;
    //     float next = wheel(src + rspeed, min, max);
    //     float prev = wheel(src - rspeed, min, max);
    //     boolean increase = wcmp(next, target, min, max) < wcmp(prev, target, min, max);
    //     return increase ? next : prev;
    // }
    //
    // // modulo but with a minimal value (instead of 0)
    // private static float wheel(float value, float min, float max) {
    //     return mod(value - min, max - min) + min;
    // }
    //
    // // Returns how close the value is to the target if limited by min and max (parameters need to be in the range [min, max])
    // private static float wcmp(float value, float target, float min, float max) {
    //     float size = max - min;
    //     float v1 = abs(target - value - size);
    //     float v2 = abs(target - value);
    //     float v3 = abs(target - value + size);
    //     return min(v1, min(v2, v3));
    // }
    //
    // // java's modulo is kinda stupid if given a negative value
    // private static float mod(float value, float limit) {
    //     float r = value % limit;
    //     if (r < 0) r += limit;
    //     return r;
    // }

    // Converge to the center at the specified speed.
    private static float converge(float value, float center, float speed) {
        float sgn = signum(value);
        float va = abs(value - center);
        float sa = min(va, abs(speed));
        return (va - sa) * sgn + center;
    }

    private static boolean canMoveDistance(AxisAlignedBB box, float dist, EnumFacing.Axis axis, Collection<AxisAlignedBB> obstacles) {
        Vec3d d = getDirectionVec(axis).scale(dist);
        AxisAlignedBB expanded = box.expand(d.x, d.y, d.z);
        return obstacles.stream().allMatch(c -> (box.intersects(c) && expanded.getCenter().squareDistanceTo(c.getCenter()) > box.getCenter().squareDistanceTo(c.getCenter())) || !expanded.intersects(c));
    }

    public static class Path {
        private float prevProgress;
        private float progress;
        private float prevXOffset;
        private float xOffset;
        private float prevRotation;
        private float rotation;

        public Path() {
            progress = prevProgress = 0.5f;
            xOffset = prevXOffset = 0f;
            rotation = prevRotation = 0f;
        }

        @Nonnull
        public Matrix4f transform(float partialTicks) {
            return new Matrix4f()
                .translate(new Vector3f(interp(prevXOffset, xOffset, partialTicks), 0f, interp(prevProgress, progress, partialTicks) - 0.5f))
                .rotate((float) Math.toRadians(interp(prevRotation, rotation, partialTicks)), new Vector3f(0f, 1f, 0f));
        }

        public boolean move(IConveyorObject obj, EnumFacing.Axis axis, Collection<AxisAlignedBB> world, float speed, float rotationSpeed) {
            prevProgress = progress;
            prevRotation = rotation;
            prevXOffset = xOffset;

            boolean check = obj.bounds().stream().allMatch(c -> canMoveDistance(c.offset(getDirectionVec(axis).scale(progress - 0.5)), speed, axis, world));

            if (check) {
                progress += speed;
                xOffset = converge(xOffset, 0, speed);
                rotation += rotationSpeed;
            }
            return check;
        }

        public float getProgress() {
            return progress;
        }

        public float getRotation() {
            return rotation;
        }

        public float getXOffset() {
            return xOffset;
        }

        public boolean canTransferToNext(float speed) {
            float p = (this.progress - 0.5f) * 2f;
            return speed * p > 0 && abs(p) > 1;
        }

        public void loadData(NBTTagCompound nbt) {
            progress = nbt.getFloat("a");
            prevProgress = nbt.getFloat("A");
            xOffset = nbt.getFloat("b");
            prevXOffset = nbt.getFloat("B");
            rotation = nbt.getFloat("c");
            prevRotation = nbt.getFloat("C");
        }

        public void saveData(NBTTagCompound nbt) {
            nbt.setFloat("a", progress);
            nbt.setFloat("A", prevProgress);
            nbt.setFloat("b", xOffset);
            nbt.setFloat("B", prevXOffset);
            nbt.setFloat("c", rotation);
            nbt.setFloat("C", prevRotation);
        }

        public static Path createFromNBT(NBTTagCompound nbt) {
            Path path = new Path();
            path.loadData(nbt);
            return path;
        }
    }
}

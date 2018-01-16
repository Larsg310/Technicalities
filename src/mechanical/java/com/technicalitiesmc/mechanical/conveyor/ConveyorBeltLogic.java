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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ConveyorBeltLogic implements IConveyorBelt {
    private final IConveyorBeltHost host;
    private final float height;

    private final Map<UUID, Pair<IConveyorObject, IPath>> objects = new HashMap<>();

    public ConveyorBeltLogic(IConveyorBeltHost host, float height) {
        this.host = host;
        this.height = height;
    }

    public void tick() {
        World world = host.getWorld();
        BlockPos pos = host.getPos();
        EnumFacing.Axis axis = host.getMovementAxis();

        if (axis == EnumFacing.Axis.Y) return; // wtf?

        for (Pair<IConveyorObject, IPath> o : Lists.newArrayList(objects.values())) {
            IConveyorObject co = o.getLeft();
            IPath p = o.getRight();
            UUID uuid = co.uuid();

            p.move(host.getMovementSpeed());
            if (p.canTransferToNext(host.getMovementSpeed())) {
                EnumFacing f = host.getEjectFacing();
                IConveyorBelt next = host.getNeighbor(f);
                if (next != null && next.canInput(f.getOpposite())) {
                    next.insert(f.getOpposite(), co);
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
                            IPath path = new PathStraight(0.5f);
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
        IPath path;
        if (side.getAxis() == getOrientation()) {
            path = new PathStraight(side.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? 1 : 0);
        } else {
            path = new PathCurved(side.getAxisDirection().getOffset() > 0 ^ getOrientation() == EnumFacing.Axis.X);
        }
        objects.put(object.uuid(), Pair.of(object, path));
        host.notifyObjectAdd(object.uuid());
    }

    public Map<UUID, Pair<IConveyorObject, IPath>> getObjects() {
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

    private static float interp(float a, float b, float partialTicks) { return a + (b - a) * partialTicks; }

    public static interface IPath {
        public static final int TYPE_STRAIGHT = 0;
        public static final int TYPE_CURVED = 1;

        @SideOnly(Side.CLIENT)
        @Nonnull
        public Matrix4f transform(float partialTicks);

        public void move(float speed);

        public boolean canTransferToNext(float speed);

        public int type();

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
        public void move(float speed) {
            prevProgress = progress;
            progress += speed;
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
            nbt.setFloat("id", type());
            nbt.setFloat("a", prevProgress);
            nbt.setFloat("b", progress);
        }

        @Override
        public int type() {
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
        public void move(float speed) {
            super.move(speed);
            prevOffsetX = offsetX;
            prevRotation = rotation;
            offsetX = converge(offsetX, speed);
            rotation = converge(rotation, 90f * speed / 0.5f);
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
        public int type() {
            return TYPE_CURVED;
        }
    }
}
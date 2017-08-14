package com.technicalitiesmc.pneumatics.tube;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;

import com.technicalitiesmc.api.pneumatics.EnumTubeDirection;
import com.technicalitiesmc.api.pneumatics.ITubeStack;
import com.technicalitiesmc.api.pneumatics.TubeModule;
import com.technicalitiesmc.pneumatics.TKPneumatics;
import com.technicalitiesmc.pneumatics.network.PacketStackPickRoute;
import com.technicalitiesmc.pneumatics.network.PacketStackUpdate;
import com.technicalitiesmc.pneumatics.tile.TilePneumaticTubeBase;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;

public class TubeStack implements ITubeStack {

    public static final float SPEED = 1 / 20F;

    private TilePneumaticTubeBase tube;
    private EnumFacing from, to;
    private float progress;

    private ItemStack stack;
    private EnumDyeColor color;

    private long id = -1;

    public TubeStack(TilePneumaticTubeBase tube, EnumFacing side, float position, EnumTubeDirection direction, ItemStack stack,
            EnumDyeColor color) {
        this.tube = tube;

        if (direction == EnumTubeDirection.INWARDS) {
            this.from = side;
            this.progress = position * 0.5F;
        } else {
            this.to = side;
            this.progress = position * 0.5F + 0.5F;
        }

        this.stack = stack;
        this.color = color;
    }

    // private TubeStack(TubeStack stack) {
    // this.tube = stack.tube;
    // this.from = stack.from;
    // this.to = stack.to;
    // this.progress = stack.progress;
    // this.stack = stack.stack;
    // this.color = stack.color;
    // }

    public TilePneumaticTubeBase getTube() {
        return tube;
    }

    @Override
    public ItemStack getStack() {
        return stack;
    }

    @Override
    public EnumDyeColor getColor() {
        return color;
    }

    public EnumFacing getFrom() {
        return from;
    }

    public EnumFacing getTo() {
        return to;
    }

    public float getProgress() {
        return progress;
    }

    @Override
    public TubeStack withStack(ItemStack stack) {
        this.stack = stack;
        return this;
    }

    @Override
    public TubeStack withColor(EnumDyeColor color) {
        this.color = color;
        return this;
    }

    public void setTube(TilePneumaticTubeBase tube) {
        this.tube = tube;
    }

    public boolean update(Supplier<EnumFacing> nextDirection) {
        float newProgress = progress + SPEED;
        if (progress <= 0.5F && newProgress >= 0.5F) {
            Set<EnumFacing> directions = EnumSet.noneOf(EnumFacing.class);
            int currentPriority = Integer.MIN_VALUE;
            for (EnumFacing dir : EnumFacing.VALUES) {
                if (dir == from) {
                    continue;
                }
                if (!tube.isConnected(dir)) {
                    continue;
                }
                TubeModule module = tube.getModule(dir);
                if (module != null && !module.canStackTraverse(this, EnumTubeDirection.OUTWARDS)) {
                    continue;
                }
                int priority = dir == from.getOpposite() ? 1 : 0;
                if (tube.getModule(dir) != null) {
                    priority += tube.getModule(dir).getTraversalPriority(this);
                }
                if (priority > currentPriority) {
                    currentPriority = priority;
                    directions.clear();
                } else if (priority < currentPriority) {
                    continue;
                }
                directions.add(dir);
            }
            if (directions.size() == 0) {
                return true;
            } else if (directions.size() == 1) {
                to = directions.iterator().next();
            } else {
                if (nextDirection == null) {
                    Iterator<EnumFacing> it = directions.iterator();
                    if (directions.size() > 1) {
                        int rnd = (int) Math.floor(Math.random() * directions.size());
                        for (int i = 0; i < rnd; i++) {
                            it.next();
                        }
                    }
                    to = it.next();
                    TKPneumatics.NETWORK_HANDLER.sendToAllAround(new PacketStackPickRoute(this), tube.getWorld(), tube.getPos());
                } else {
                    to = nextDirection.get();
                    if (to == null) {
                        progress = 0.5F;
                        return false;
                    }
                }
            }
        }

        if (!tube.getWorld().isRemote) {
            EnumFacing currentSide = progress < 0.5F ? from : to;
            if (currentSide != null) {
                TubeModule module = tube.getModule(currentSide);
                if (module != null) {
                    float pos = module.getPosition() * 0.25F;
                    if (progress >= 0.5F) {
                        pos = 1.0F - pos;
                    }
                    if (progress < pos && newProgress >= pos) {
                        module.traverse(this, pos < 0.5F ? EnumTubeDirection.INWARDS : EnumTubeDirection.OUTWARDS);
                        TKPneumatics.NETWORK_HANDLER.sendToAllAround(new PacketStackUpdate(this), tube.getWorld(), tube.getPos());
                    }
                }
            }
        }

        progress = newProgress;
        return false;
    }

    public void transferTo(TilePneumaticTubeBase newTube) {
        this.tube.stacks.remove(this);
        this.tube.markDirty();
        this.tube = newTube;
        this.from = to.getOpposite();
        this.to = null;
        this.progress -= 1.0F;
        newTube.stacks.add(this);
        newTube.markDirty();
    }

    public void update(ItemStack stack, EnumDyeColor color) {
        this.stack = stack;
        this.color = color;
    }

    public long getID() {
        return id;
    }

    public void setID(long id) {
        this.id = id;
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setByte("from", (byte) from.ordinal());
        if (to != null) {
            tag.setByte("to", (byte) to.ordinal());
        }
        tag.setFloat("prog", progress);

        tag.setTag("stack", stack.serializeNBT());
        if (color != null) {
            tag.setByte("col", (byte) color.getMetadata());
        }

        tag.setLong("id", id);

        return tag;
    }

    public static TubeStack deserializeNBT(TilePneumaticTubeBase tube, NBTTagCompound tag) {
        EnumFacing from = EnumFacing.getFront(tag.getInteger("from") & 0xFF);
        EnumFacing to = !tag.hasKey("to") ? null : EnumFacing.getFront(tag.getInteger("to") & 0xFF);
        float progress = tag.getFloat("prog");

        ItemStack stack = new ItemStack(tag.getCompoundTag("stack"));
        EnumDyeColor color = !tag.hasKey("col") ? null : EnumDyeColor.byMetadata(tag.getInteger("col") & 0xFF);

        long id = tag.getLong("id");

        TubeStack tubeStack = new TubeStack(tube, from, progress, EnumTubeDirection.INWARDS, stack, color);
        tubeStack.to = to;
        tubeStack.id = id;
        return tubeStack;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeByte(from.ordinal());
        if (to != null) {
            buf.writeBoolean(true);
            buf.writeByte(to.ordinal());
        } else {
            buf.writeBoolean(false);
        }
        buf.writeFloat(progress);

        buf.writeItemStack(stack);
        if (color != null) {
            buf.writeBoolean(true);
            buf.writeByte(color.ordinal());
        } else {
            buf.writeBoolean(false);
        }

        buf.writeLong(id);
    }

    public static TubeStack fromBytes(TilePneumaticTubeBase tube, PacketBuffer buf) {
        try {
            EnumFacing from = EnumFacing.getFront(buf.readByte() & 0xFF);
            EnumFacing to = null;
            if (buf.readBoolean()) {
                to = EnumFacing.getFront(buf.readByte() & 0xFF);
            }
            float progress = buf.readFloat();

            ItemStack stack = buf.readItemStack();
            EnumDyeColor color = buf.readBoolean() ? EnumDyeColor.byMetadata(buf.readByte() & 0xFF) : null;

            long id = buf.readLong();

            TubeStack tubeStack = new TubeStack(tube, from, progress, EnumTubeDirection.INWARDS, stack, color);
            tubeStack.to = to;
            tubeStack.id = id;
            return tubeStack;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}

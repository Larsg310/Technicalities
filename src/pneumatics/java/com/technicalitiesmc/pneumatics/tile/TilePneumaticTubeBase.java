package com.technicalitiesmc.pneumatics.tile;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.tuple.Triple;

import com.technicalitiesmc.api.pneumatics.IPneumaticTube;
import com.technicalitiesmc.api.pneumatics.TubeModule;
import com.technicalitiesmc.pneumatics.tube.TubeStack;
import com.technicalitiesmc.pneumatics.tube.module.ModuleManager;
import com.technicalitiesmc.util.block.TileBase;
import com.technicalitiesmc.util.simple.SimpleCapability;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.Constants.NBT;

@SimpleCapability
public abstract class TilePneumaticTubeBase extends TileBase implements IPneumaticTube {

    @CapabilityInject(TilePneumaticTubeBase.class)
    public static final Capability<TilePneumaticTubeBase> CAPABILITY = null;

    public final Set<TubeStack> stacks = Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected final Map<EnumFacing, Triple<TubeModule.Type<?>, TubeModule, TubeModule>> modules = new EnumMap<>(EnumFacing.class);

    @Override
    public World getTubeWorld() {
        return getWorld();
    }

    @Override
    public BlockPos getTubePos() {
        return getPos();
    }

    public abstract <T extends TubeModule> boolean setModule(EnumFacing face, TubeModule.Type<T> type);

    public TubeModule.Type<?> getModuleType(EnumFacing face) {
        Triple<TubeModule.Type<?>, TubeModule, TubeModule> triple = modules.get(face);
        return triple != null ? triple.getLeft() : null;
    }

    @SuppressWarnings("unchecked")
    public <T extends TubeModule> TubeModule getModule(EnumFacing face) {
        Triple<TubeModule.Type<?>, TubeModule, TubeModule> triple = modules.get(face);
        if (triple == null) {
            return null;
        }

        if (triple.getMiddle() == triple.getRight()) {
            TileEntity te = world.getTileEntity(pos.offset(face));
            if (te == null || !te.hasCapability(CAPABILITY, null)) {
                modules.remove(face);
                markDirty();
                return null;
            }
            TilePneumaticTubeBase tube = te.getCapability(CAPABILITY, null);
            Triple<TubeModule.Type<?>, TubeModule, TubeModule> other = tube.modules.get(face.getOpposite());
            if (other == null || triple.getLeft() != other.getLeft()) {
                return null;
            }
            ((TubeModule.Type<T>) triple.getLeft()).link((T) triple.getMiddle(), (T) other.getMiddle());

            modules.put(face, Triple.of(triple.getLeft(), triple.getMiddle(), other.getMiddle()));
            tube.modules.put(face.getOpposite(), Triple.of(triple.getLeft(), other.getMiddle(), triple.getMiddle()));
        }

        return triple.getMiddle();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CAPABILITY || super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CAPABILITY) {
            return (T) this;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);

        NBTTagList modules = new NBTTagList();
        for (EnumFacing face : EnumFacing.VALUES) {
            NBTTagCompound t = new NBTTagCompound();
            Triple<TubeModule.Type<?>, TubeModule, TubeModule> module = this.modules.get(face);
            if (module != null) {
                t.setInteger("__type", ModuleManager.INSTANCE.getID(module.getLeft()));
                t.setBoolean("__pair", module.getRight() != null && module.getRight() != module.getMiddle());
                t = module.getMiddle().writeToNBT(t);
            }
            modules.appendTag(t);
        }
        tag.setTag("modules", modules);

        NBTTagList stacks = new NBTTagList();
        this.stacks.forEach(s -> stacks.appendTag(s.serializeNBT()));
        tag.setTag("stacks", stacks);

        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        NBTTagList modules = tag.getTagList("modules", NBT.TAG_COMPOUND);
        for (EnumFacing face : EnumFacing.VALUES) {
            NBTTagCompound t = modules.getCompoundTagAt(face.ordinal());
            if (t.hasKey("__type")) {
                TubeModule.Type<?> currentType = getModuleType(face);
                if (currentType != null && ModuleManager.INSTANCE.getID(currentType) == t.getInteger("__type")) {
                    TubeModule module = getModule(face);
                    module.readFromNBT(t);
                } else {
                    TubeModule.Type<?> type = ModuleManager.INSTANCE.get(t.getInteger("__type"));
                    TubeModule module = type.instantiate(this, face);
                    module.readFromNBT(t);
                    this.modules.put(face, Triple.of(type, module, t.getBoolean("__pair") ? module : null));
                }
            } else {
                this.modules.remove(face);
            }
        }

        stacks.clear();
        NBTTagList stacks = tag.getTagList("stacks", NBT.TAG_COMPOUND);
        stacks.forEach(t -> this.stacks.add(TubeStack.deserializeNBT(this, (NBTTagCompound) t)));
    }

    @Override
    public void writeDescription(PacketBuffer buf) {
        for (EnumFacing face : EnumFacing.VALUES) {
            Triple<TubeModule.Type<?>, TubeModule, TubeModule> module = this.modules.get(face);
            if (module != null) {
                buf.writeBoolean(true);
                buf.writeInt(ModuleManager.INSTANCE.getID(module.getLeft()));
                buf.writeBoolean(module.getRight() != null && module.getRight() != module.getMiddle());
                module.getMiddle().writeUpdateExtra(buf);
            } else {
                buf.writeBoolean(false);
            }
        }
    }

    @Override
    public void readDescription(PacketBuffer buf) {
        for (EnumFacing face : EnumFacing.VALUES) {
            if (!buf.readBoolean()) {
                this.modules.remove(face);
                continue;
            }

            TubeModule.Type<?> type = ModuleManager.INSTANCE.get(buf.readInt());
            TubeModule.Type<?> currentType = getModuleType(face);
            boolean isPair = buf.readBoolean();

            if (type == currentType) {
                TubeModule module = getModule(face);
                if (module != null) {
                    module.readUpdateExtra(buf);
                    continue;
                }
            }
            TubeModule module = type.instantiate(this, face);
            module.readUpdateExtra(buf);
            this.modules.put(face, Triple.of(type, module, isPair ? module : null));
        }

        if (getWorld() != null) {
            getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
        }
    }

}

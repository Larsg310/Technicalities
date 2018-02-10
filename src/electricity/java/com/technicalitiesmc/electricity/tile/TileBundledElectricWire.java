package com.technicalitiesmc.electricity.tile;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.technicalitiesmc.electricity.init.BlockRegister;
import com.technicalitiesmc.electricity.util.EnumBitSet;
import com.technicalitiesmc.lib.IndexedAABB;
import com.technicalitiesmc.lib.block.TileBase;
import elec332.core.main.ElecCore;
import elec332.core.tile.TileEntityBase;
import elec332.core.util.NBTTypes;
import elec332.core.world.WorldHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by Elec332 on 19-1-2018.
 */
public class TileBundledElectricWire extends TileBase {

    public static final EnumSet<EnumFacing> NS, EW;

    static {
        NS = EnumSet.of(EnumFacing.SOUTH, EnumFacing.NORTH);
        EW = EnumSet.of(EnumFacing.EAST, EnumFacing.WEST);
    }

    private final List<WirePart> wires = Lists.newArrayList();
    private final List<WirePart> wirez = Collections.unmodifiableList(wires);
    private boolean pingpong, send;
    private long worldTime;
    private Set<BlockPos> posSet = Sets.newHashSet();

    public boolean shouldRefresh(long newTime, BlockPos otherPos){
        if (worldTime != newTime){
            posSet.clear();
            worldTime = newTime;
            posSet.add(otherPos);
            return true;
        }
        if (posSet.contains(otherPos)){
            return false;
        }
        posSet.add(otherPos);
        return true;
    }

    public boolean addWire(WirePart wire){
        return addWire(wire, true);
    }

    private boolean addWire(WirePart wire, boolean notify){
        if (getWire(wire.getPlacement()) == null){
            if (notify){
                IBlockState state = WorldHelper.getBlockState(world, pos);
                List<AxisAlignedBB> abl = Lists.newArrayList(), abs = Lists.newArrayList();
                wire.addBoxes(abl, true, Collections.emptySet(), true, false);
                for (AxisAlignedBB bb : abl) {
                    abs.clear();
                    if (bb instanceof IndexedAABB){
                        bb = new IndexedAABB(bb, ((IndexedAABB) bb).index + 10);
                    }
                    state.addCollisionBoxToList(world, pos, bb.offset(pos), abs, null, false);
                    if (!abs.isEmpty()){
                        return false;
                    }
                }
            }
            wires.add(wire);
            wire.wire = this;
            if (world != null && !world.isRemote){
                wire.pong(pos, world);
                if (notify) {
                    notifyNeighborsOfChangeExtensively();
                    markDirty();
                }
            }
            return true;
        }
        return false;
    }

    public List<WirePart> getWireView() {
        return wirez;
    }

    public void removeAll(Collection<WirePart> wireParts){
        wires.removeAll(wireParts);
        onWiresRemoved();
    }

    public void remove(WirePart wire){
        wires.remove(wire);
        onWiresRemoved();
    }

    private void onWiresRemoved(){
        if (world.isRemote){
            return;
        }
        markDirty();
        if (wires.isEmpty()){
            world.setBlockToAir(pos);
        } else {
            ping();
        }
    }

    @Nullable
    public WirePart getWire(EnumFacing facing){
        for (WirePart wire : wires){
            if (wire.getPlacement() == facing){
                return wire;
            }
        }
        return null;
    }

    public void notifyNeighborsOfChangeExtensively(){
        BlockPos start = pos.add(-1, -1, -1);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    world.neighborChanged(start.add(i, j, k), BlockRegister.electric_bundled_wire, pos);
                }
            }
        }
    }

    public void ping(){
        pingpong = true;
        wires.forEach(wirePart -> wirePart.pong(pos, world));
        pingpong = false;
        if (send){
            send = false;
            syncWireData();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        readWiresFromNBT(compound, false);
    }

    private void readWiresFromNBT(NBTTagCompound compound, boolean client){
        NBTTagList l = compound.getTagList("wirestuff", NBTTypes.COMPOUND.getID());
        EnumBitSet<EnumFacing> faces = EnumBitSet.noneOf(EnumFacing.class);
        for (int i = 0; i < l.tagCount(); i++) {
            NBTTagCompound tag = l.getCompoundTagAt(i);
            WirePart wirePart = new WirePart(EnumFacing.VALUES[tag.getByte("sbfww")], tag.getByte("sbfws"));
            if (client){
                WirePart w = getWire(wirePart.getPlacement());
                if (w != null){
                    wirePart = w;
                }
            }
            wirePart.readFromNBT(tag);
            if (client){
                wirePart.readClientData(tag);
            }
            faces.add(wirePart.getPlacement());
            addWire(wirePart, false);
        }
        if (client){
            List<WirePart> wp = Lists.newArrayList();
            for (WirePart p : getWireView()){
                if (!faces.contains(p.getPlacement())){
                    wp.add(p);
                }
            }
            removeAll(wp);
        }
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        return writeWiresToNBT(super.writeToNBT(compound), false);
    }

    private NBTTagCompound writeWiresToNBT(NBTTagCompound ret, boolean client){
        NBTTagList l = new NBTTagList();
        wires.forEach(wire -> {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setByte("sbfww", (byte) wire.getPlacement().ordinal());
            tag.setByte("sbfws", (byte) wire.getWireSize());
            wire.writeToNBT(tag);
            if (client){
                wire.writeClientData(tag);
            }
            l.appendTag(tag);
        });
        ret.setTag("wirestuff", l);
        return ret;
    }

    public void syncWireData(){
        if (pingpong){
            send = true;
            return;
        }
        try {
            Field f = TileEntityBase.class.getDeclaredField("isGatheringPackets");
            f.setAccessible(true);
            if (!f.getBoolean(this)){
                System.out.println("sendRealPacket "+pos);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        sendPacket(3, writeWiresToNBT(new NBTTagCompound(), true));
    }

    @Override
    public void onDataPacket(int id, NBTTagCompound tag) {
        if (id == 3){
            readWiresFromNBT(tag, true);
            world.markBlockRangeForRenderUpdate(pos.add(1, 1, 1), pos.add(-1, -1, -1));
        } else {
            super.onDataPacket(id, tag);
        }
    }

    @Override
    public void onLoad() {
        if (!world.isRemote) {
            ElecCore.tickHandler.registerCall(this::ping, world);
        }
    }

    @Override
    public void sendInitialLoadPackets() {
        syncWireData();
    }

    public static boolean isStraightLine(Set<EnumFacing> connections){
        return connections.equals(NS) || connections.equals(EW);
    }

}

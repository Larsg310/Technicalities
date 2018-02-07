package com.technicalitiesmc.electricity.tile;

import com.google.common.collect.Lists;
import com.technicalitiesmc.electricity.init.BlockRegister;
import com.technicalitiesmc.electricity.util.EnumBitSet;
import com.technicalitiesmc.lib.block.TileBase;
import elec332.core.main.ElecCore;
import elec332.core.util.NBTTypes;
import elec332.core.world.WorldHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Elec332 on 19-1-2018.
 */
public class TileBundledElectricWire extends TileBase {

    private static final Set<EnumFacing> surroundings;
    private static final EnumFacing[][] beep;


    public static final EnumSet<EnumFacing> NS, EW;


    static {
        List<EnumFacing> dirs = Lists.newArrayList(EnumFacing.VALUES);
        surroundings = dirs.stream().filter(facing -> facing.getAxis() != EnumFacing.Axis.Y).collect(Collectors.toSet());
        beep = new EnumFacing[dirs.size()][];
        dirs.forEach(new Consumer<EnumFacing>() {
            @Override
            public void accept(EnumFacing facing) {
                beep[facing.ordinal()] = dirs.stream().filter(f -> f.getAxis() != facing.getAxis()).toArray(EnumFacing[]::new);
            }
        });
        NS = EnumSet.of(EnumFacing.SOUTH, EnumFacing.NORTH);
        EW = EnumSet.of(EnumFacing.EAST, EnumFacing.WEST);
    }

    private final List<WirePart> wires = Lists.newArrayList();
    private final List<WirePart> wirez = Collections.unmodifiableList(wires);
    private boolean pingpong, send;

    public boolean addWire(WirePart wire){
        return addWire(wire, true);
    }

    private boolean addWire(WirePart wire, boolean notify){
        if (getWire(wire.placement) == null){
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
            if (wire.placement == facing){
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
            WirePart wirePart = new WirePart(EnumFacing.VALUES[tag.getByte("sbfww")]);
            if (client){
                WirePart w = getWire(wirePart.placement);
                if (w != null){
                    wirePart = w;
                }
            }
            wirePart.readFromNBT(tag);
            if (client){
                wirePart.readClientData(tag);
            }
            faces.add(wirePart.placement);
            addWire(wirePart, false);
        }
        if (client){
            List<WirePart> wp = Lists.newArrayList();
            for (WirePart p : getWireView()){
                if (!faces.contains(p.placement)){
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
            tag.setByte("sbfww", (byte) wire.placement.ordinal());
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

    /*

    @SuppressWarnings("all")
    private void syncColors(boolean conn){
        checkConnections(!conn);
        if (!world.isRemote) {
            NBTTagCompound tag = new NBTTagCompound();
            if (conn){
                tag.setLong("conn", connections.getSerialized());
                tag.setLong("ch", change.getSerialized());
            }
            tag.setInteger("color", colors);
            sendPacket(3, tag);
        }
    }

    private void syncConnections() {
        if (!world.isRemote) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setLong("conn", connections.getSerialized());
            tag.setLong("ch", change.getSerialized());
            tag.setByteArray("baoc", ud.toByteArray());
            sendPacket(4, tag);
        }
    }

    @Override
    public void onLoad() {
        if (!world.isRemote) {
            //ElecCore.tickHandler.registerCall(new Runnable() {
            //    @Override
            //    public void run() {
                    checkConnections(false);
            //    }
            //}, world);
        }
    }

    public void checkConnections(boolean send){
        if (world.isRemote){
            return;
        }
        connections.clear();
        change.clear();
        ud.clear();
        for (EnumFacing facing : EnumFacing.VALUES){
            if (facing.getAxis() == EnumFacing.Axis.Y){
                continue;
            }
            BlockPos pos = this.pos.offset(facing);
            if (WorldHelper.chunkLoaded(world, pos)) {
                TileEntity tile = WorldHelper.getTileAt(world, pos);
                if (tile instanceof TileBundledElectricWire) {
                    TileBundledElectricWire wire = (TileBundledElectricWire) tile;
                    if (JavaHelper.hasAtLeastOneMatch(ColorHelper.getColors(wire.getColorBits()), ColorHelper.getColors(getColorBits()))) {
                        connections.add(facing);
                        if (((TileBundledElectricWire) tile).colors != colors) {
                            change.add(facing);
                        }
                    }
                } else {
                    TileBundledElectricWire wire;
                    if ((wire = connectUp(facing)) != null){
                        connections.add(facing);
                        ud.set(facing.ordinal());
                        if (wire.colors != colors) {
                            change.add(facing);
                        }
                    } else {
                        tile = WorldHelper.getTileAt(world, pos.down());
                        if (tile instanceof TileBundledElectricWire && ((TileBundledElectricWire) tile).connectUp(facing.getOpposite()) == this){
                            connections.add(facing);
                            if (((TileBundledElectricWire) tile).colors != colors) {
                                change.add(facing);
                            }
                        }
                    }
                }
            }
        }
        if (send) {
            syncConnections();
        }
    }

    private TileBundledElectricWire connectUp(EnumFacing facing){
        BlockPos pos = this.pos.offset(facing);
        IBlockState state = WorldHelper.getBlockState(world, pos);
        if (!state.isSideSolid(world, pos, facing.getOpposite())){
            return null;
        }
        pos = pos.up();
        TileEntity tile = WorldHelper.getTileAt(world, pos);
        if (tile instanceof TileBundledElectricWire) {
            TileBundledElectricWire wire = (TileBundledElectricWire) tile;
            if (JavaHelper.hasAtLeastOneMatch(ColorHelper.getColors(wire.getColorBits()), ColorHelper.getColors(getColorBits()))) {
                state = WorldHelper.getBlockState(world, pos.offset(facing.getOpposite()));
                if (!state.isFullBlock() && !state.isFullCube()) {
                    return (TileBundledElectricWire) tile;
                }
            }
        }
        return null;
    }

    @Override
    public void sendInitialLoadPackets() {
        checkConnections(true);
    }

    @Override
    public void onDataPacket(int id, NBTTagCompound tag) {
        switch (id){
            case 3:
                int newColor = tag.getInteger("color");
                if (newColor != colors){
                    this.colors = newColor;
                    if (!tag.hasKey("conn")) {
                        doWhackyClientStuff();
                        break;
                    }
                }
            case 4:
                long newConn = tag.getLong("conn");
                boolean cg = false;
                if (newConn != connections.getSerialized()){
                    connections.deserialize(newConn);
                    cg = true;
                }
                long ch = tag.getLong("ch");
                if (ch != change.getSerialized()){
                    change.deserialize(ch);
                    cg = true;
                }
                byte[] ba = tag.getByteArray("baoc");
                if (!Arrays.equals(ba, ud.toByteArray())){
                    ud = BitSet.valueOf(ba);
                    cg = true;
                }
                if (cg){
                    doWhackyClientStuff();
                }
                break;
            default:
                super.onDataPacket(id, tag);
        }
    }*/

    private void doWhackyClientStuff(){
        //notifyNeighborsOfChange();
        WorldHelper.markBlockForUpdate(world, pos);
        //WorldHelper.markBlockForRenderUpdate(world, pos);
    }

    public static boolean isStraightLine(Set<EnumFacing> connections){
        return connections.equals(NS) || connections.equals(EW);
    }


}

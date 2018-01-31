package com.technicalitiesmc.electricity.tile;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.technicalitiesmc.electricity.util.ColorHelper;
import com.technicalitiesmc.electricity.util.EnumBitSet;
import com.technicalitiesmc.electricity.util.WireColor;
import com.technicalitiesmc.lib.block.TileBase;
import elec332.core.java.JavaHelper;
import elec332.core.main.ElecCore;
import elec332.core.util.NBTHelper;
import elec332.core.world.WorldHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
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
                beep[facing.ordinal()] = dirs.stream().filter(f -> f.getAxis() != facing.getAxis()).collect(Collectors.toList()).toArray(new EnumFacing[0]);
            }
        });
        NS = EnumSet.of(EnumFacing.SOUTH, EnumFacing.NORTH);
        EW = EnumSet.of(EnumFacing.EAST, EnumFacing.WEST);
    }

    public List<WirePart> wires;



    public void notifyNeighborsOfChangeExtensively(){
        WorldHelper.notifyNeighborsOfStateChange(this.getWorld(), this.pos.up(), this.blockType);
        notifyNeighborsOfChange();
        WorldHelper.notifyNeighborsOfStateChange(this.getWorld(), this.pos.down(), this.blockType);
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

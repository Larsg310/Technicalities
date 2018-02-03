package com.technicalitiesmc.electricity.tile;

import com.google.common.collect.Sets;
import com.technicalitiesmc.electricity.item.ItemBundledWire;
import com.technicalitiesmc.electricity.util.ColorHelper;
import com.technicalitiesmc.electricity.util.EnumBitSet;
import com.technicalitiesmc.electricity.util.WireColor;
import com.technicalitiesmc.lib.AABBHelper;
import com.technicalitiesmc.lib.IndexedAABB;
import elec332.core.java.JavaHelper;
import elec332.core.world.WorldHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Created by Elec332 on 25-1-2018.
 */
public class WirePart {

    public WirePart(EnumFacing placement){
        this.placement = placement;
    }

    public EnumFacing placement;

    protected TileBundledElectricWire wire;

    private static final EnumFacing[] indexToFacing, st1, st2;
    private static final EnumFacing[][] placementToIndex;

    private int colors = 0;
    public EnumBitSet<EnumFacing> connections = EnumBitSet.noneOf(EnumFacing.class), change = EnumBitSet.noneOf(EnumFacing.class), realConnections = EnumBitSet.noneOf(EnumFacing.class);
    public BitSet ud = new BitSet(EnumFacing.VALUES.length);

    @Nonnull
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("wrclr", colors);
        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        this.colors = compound.getInteger("wrclr");
    }

    public void writeClientData(NBTTagCompound tag){
        System.out.println("send "+wire.getPos());
        tag.setLong("conn", connections.getSerialized());
        tag.setLong("change", change.getSerialized());
        int i = new Random().nextInt();
        System.out.println(i);
        tag.setInteger("inte", i);
    }

    @SideOnly(Side.CLIENT)
    public void readClientData(NBTTagCompound tag){
        System.out.println("receiuve");
        connections = EnumBitSet.noneOf(EnumFacing.class);
        connections.deserialize(tag.getLong("conn"));
        change.deserialize(tag.getLong("change"));
        System.out.println(tag.getInteger("inte"));
    }

    public boolean canStay(World world, BlockPos pos){
        return WorldHelper.getBlockState(world, pos.offset(placement)).isSideSolid(world, pos.offset(placement), placement.getOpposite());
    }

    public ItemStack getDropStack(){
        return ItemBundledWire.withCables(ColorHelper.getColors(getColorBits()));
    }

    public void pong(BlockPos pos_, World world){
        System.out.println("pong "+pos_);
        connections.clear();
        change.clear();
        realConnections.clear();
        for (int i = 0; i < 4; i++) {
            EnumFacing facing = placementToIndex[placement.ordinal()][i];

            BlockPos pos = pos_.offset(facing);
            if (WorldHelper.chunkLoaded(world, pos)) {
                TileEntity tile = WorldHelper.getTileAt(world, pos);
                if (tile instanceof TileBundledElectricWire) {
                    TileBundledElectricWire wireO = (TileBundledElectricWire) tile;
                    WirePart oWp = wireO.getWire(placement);
                    if (oWp != null && JavaHelper.hasAtLeastOneMatch(ColorHelper.getColors(oWp.getColorBits()), ColorHelper.getColors(getColorBits()))) {
                        connections.add(indexToFacing[i]);
                        if (oWp.colors != colors) {
                            change.add(indexToFacing[i]);
                        }
                        realConnections.add(facing);
                    }
                }
            }
        }
        wire.syncWireData();
    }

    public void addBoxes(IBlockState state, World world, BlockPos pos, List<AxisAlignedBB> boxes) {
        float width = ColorHelper.getColors(getColorBits()).size();
        float stuff = ((16 - width) / 2) / 16;
        float stuff2 = .5f;
        if (connections.size() != 1) {
            if (!isStraightLine()) {
                float ft = (16 - (width + 2)) / 32f;
                boxes.add(new IndexedAABB(AABBHelper.rotateFromDown(new AxisAlignedBB(ft, 0, ft, 1 - ft, 1 / 16f, 1 - ft), placement), placement.ordinal()));
            }
            stuff2 = stuff;
        }
        for (EnumFacing facing : connections){
            boolean z = facing.getAxis() == EnumFacing.Axis.Z;
            boolean n = facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;
            AxisAlignedBB aabb = new AxisAlignedBB(z ? stuff : 1, 0, z ? 1 - stuff2 : stuff, 1 - (z ? stuff : stuff2), 1/16f, z ? 1 : 1 - stuff);
            if (n){
                float offset = -(1 - stuff2);
                aabb = aabb.offset(z ? 0 : offset, 0, z ? offset : 0);
            }
            boxes.add(new IndexedAABB(AABBHelper.rotateFromDown(aabb, placement), placement.ordinal()));
        }
    }

    public int getColorBits() {
        return colors;
    }

    public boolean isStraightLine(){
        return TileBundledElectricWire.isStraightLine(connections);
    }

    public boolean hasWire(WireColor color) {
        return ColorHelper.hasWire(color, colors);
    }

    public boolean addWires(Collection<WireColor> colorz){
        Set<WireColor> colors = Sets.newHashSet(colorz);
        if (colors.size() != colorz.size()){
            return false;
        }
        for (WireColor color : colors){
            if (hasWire(color)){
                return false;
            }
        }
        colors.forEach(this::addWire);
        cS();
        return true;
    }

    public boolean addWire(WireColor color) {
        if (hasWire(color)){
            return false;
        }
        colors = ColorHelper.addWire(color, colors);
        cS();
        return true;
    }

    private void cS(){
        if (wire != null){
            wire.notifyNeighborsOfChangeExtensively();
            wire.syncWireData();
        }
    }

    public boolean removeWire(WireColor color) {
        if (hasWire(color)) {
            colors = ColorHelper.removeWire(color, colors);
            cS();
            return true;
        }
        return false;
    }

    public void setColors(List<WireColor> colors){
        this.colors = 0;
        for (WireColor color : colors){
            addWire(color);
        }
        cS();
    }

    static {
        indexToFacing = new EnumFacing[]{
                EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST
        };
        st1 = new EnumFacing[]{
                EnumFacing.UP, EnumFacing.EAST, EnumFacing.DOWN, EnumFacing.WEST
        };
        st2 = new EnumFacing[]{
                EnumFacing.NORTH, EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.UP
        };
        placementToIndex = new EnumFacing[EnumFacing.VALUES.length][];
        for (EnumFacing placement : EnumFacing.VALUES){
            int p = placement.ordinal();
            placementToIndex[p] = new EnumFacing[4];
            for (int i = 0; i < 4; i++) {
                placementToIndex[p][i] = getFacingStuff(placement, i);
            }
        }
    }

    private static EnumFacing getFacingStuff(EnumFacing placement, int index){
        switch (placement){
            case UP:
                if (index % 2 == 0){
                    return indexToFacing[index].getOpposite();
                }
            case DOWN:
                return indexToFacing[index];
            case SOUTH:
                if (index % 2 == 0){
                    return st1[index].getOpposite();
                }
            case NORTH:
                return st1[index];
            case EAST:
                if (index % 2 == 1){
                    return st2[index].getOpposite();
                }
            case WEST:
                return st2[index];
            default:
                return null;
        }
    }

}

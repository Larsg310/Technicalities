package com.technicalitiesmc.electricity.tile;

import com.google.common.collect.Lists;
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
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Created by Elec332 on 25-1-2018.
 */
public class WirePart {

    public WirePart(EnumFacing placement){
        this.placement = placement;
        makeAABBMimicRenderLogic = false;
    }

    public EnumFacing placement;

    protected TileBundledElectricWire wire;

    private static final EnumFacing[] indexToFacing, st1, st2;
    private static final EnumFacing[][] placementToIndex, placementToIndexReverse;

    private int colors = 0;
    public EnumBitSet<EnumFacing> realConnections = EnumBitSet.noneOf(EnumFacing.class);
    public NonNullList<EnumConnectionPlace> corners = NonNullList.withSize(EnumFacing.VALUES.length, EnumConnectionPlace.NORMAL);

    public EnumBitSet<EnumFacing> connections = EnumBitSet.noneOf(EnumFacing.class), change = EnumBitSet.noneOf(EnumFacing.class);
    public BitSet extended = new BitSet(EnumFacing.VALUES.length);
    public BitSet shortened = new BitSet(EnumFacing.VALUES.length);

    private boolean makeAABBMimicRenderLogic;

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
        tag.setByteArray("extCon", extended.toByteArray());
        tag.setByteArray("shoCon", shortened.toByteArray());
        int i = new Random().nextInt();
        System.out.println(i);
        tag.setInteger("inte", i);
    }

    @SideOnly(Side.CLIENT)
    public void readClientData(NBTTagCompound tag){
        System.out.println("receiuve");
        connections.deserialize(tag.getLong("conn"));
        change.deserialize(tag.getLong("change"));
        extended = BitSet.valueOf(tag.getByteArray("extCon"));
        shortened = BitSet.valueOf(tag.getByteArray("shoCon"));
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
        realConnections.clear();
        corners.clear();
        connections.clear();
        change.clear();
        extended.clear();
        shortened.clear();
        for (int i = 0; i < 4; i++) {
            EnumFacing facing = placementToIndex[placement.ordinal()][i];
            for (EnumConnectionPlace cp : EnumConnectionPlace.values()){
                Pair<BlockPos, EnumFacing> pbf = cp.modify(world, pos_, this, facing);
                if (pbf == null){
                    continue;
                }
                BlockPos pos = pbf.getLeft();
                EnumFacing otherPlacement = pbf.getRight();
                if (WorldHelper.chunkLoaded(world, pos)) {
                    TileEntity tile = WorldHelper.getTileAt(world, pos);
                    if (tile instanceof TileBundledElectricWire) {
                        TileBundledElectricWire wireO = (TileBundledElectricWire) tile;
                        WirePart oWp = wireO.getWire(otherPlacement);
                        if (oWp != null && JavaHelper.hasAtLeastOneMatch(ColorHelper.getColors(oWp.getColorBits()), ColorHelper.getColors(getColorBits()))) {
                            connections.add(indexToFacing[i]);
                            if (oWp.colors != colors) {
                                change.add(indexToFacing[i]);
                            }
                            realConnections.add(facing);
                            corners.set(facing.ordinal(), cp);
                            if (cp == EnumConnectionPlace.CORNER_DOWN){
                                extended.set(indexToFacing[i].ordinal());
                            } else if (cp == EnumConnectionPlace.CORNER_UP){
                                shortened.set(indexToFacing[i].ordinal());
                            }
                            break;
                        }
                    }
                }
            }
        }
        wire.syncWireData();
    }

    public void addBoxes(IBlockState state, World world, BlockPos pos, List<AxisAlignedBB> boxes) {
        addBoxes(boxes, false, connections);
    }

    private void addBoxes(List<AxisAlignedBB> boxes, boolean extend, Set<EnumFacing> connections){
        float width = ColorHelper.getColors(getColorBits()).size();
        float stuff = ((16 - width) / 2) / 16;
        float stuff2 = .5f;
        if (connections.size() != 1 && !TileBundledElectricWire.isStraightLine(connections)) {
            float ft = (16 - (width + 2)) / 32f;
            boxes.add(new IndexedAABB(AABBHelper.rotateFromDown(new AxisAlignedBB(ft, 0, ft, 1 - ft, 1 / 16f, 1 - ft), placement), placement.ordinal()));
            stuff2 = stuff;
        }
        for (EnumFacing facing : connections){
            boolean z = facing.getAxis() == EnumFacing.Axis.Z;
            boolean n = facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;
            boolean ext = extend || (isExtended(facing) || !makeAABBMimicRenderLogic) && extended.get(facing.ordinal());
            boolean shrt = !makeAABBMimicRenderLogic && shortened.get(facing.ordinal()) && isExtended(facing);
            float eadd = ext ? 1/16f : (shrt ? -1/16f : 0);
            AxisAlignedBB aabb = new AxisAlignedBB(z ? stuff : 1 + eadd, 0, z ? 1 - stuff2 : stuff, 1 - (z ? stuff : stuff2), 1/16f, z ? 1 + eadd : 1 - stuff);
            if (n){
                float offset = -(1 - stuff2 + eadd);
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

    enum EnumConnectionPlace {

        CORNER_UP { // Cornering up, within the same block

            @Override
            public Pair<BlockPos, EnumFacing> modify(World world, BlockPos myPos, WirePart wire, EnumFacing to) {
                return Pair.of(myPos, to);
            }

        },
        NORMAL { //Straight forward

            @Override
            public Pair<BlockPos, EnumFacing> modify(World world, BlockPos myPos, WirePart wire, EnumFacing to) {
                return Pair.of(myPos.offset(to), wire.placement);
            }

        },
        CORNER_DOWN { //Cornering down, other block

            @Override
            @SuppressWarnings("all")
            public Pair<BlockPos, EnumFacing> modify(World world, BlockPos myPos, WirePart wire, EnumFacing to) {
                BlockPos pos = myPos.offset(to);
                IBlockState state = WorldHelper.getBlockState(world, pos);
                List<AxisAlignedBB> abl = Lists.newArrayList(), abs = Lists.newArrayList();
                Set<EnumFacing> f = wire.connections.clone();
                f.add(placementToIndexReverse[wire.placement.ordinal()][to.ordinal()]);
                wire.addBoxes(abl, true, f);
                for (AxisAlignedBB bb : abl) {
                    state.addCollisionBoxToList(world, pos, bb.offset(myPos), abs, null, false);
                    if (!abs.isEmpty()){
                        return null;
                    }
                }
                return Pair.of(pos.offset(wire.placement), to.getOpposite());
            }

        };

        @Nullable
        public abstract Pair<BlockPos, EnumFacing> modify(World world, BlockPos myPos, WirePart wire, EnumFacing to);

    }

    public boolean isExtended(EnumFacing horPaneFacing){
        return placement.getAxis() == EnumFacing.Axis.Y || placement.getAxis() == EnumFacing.Axis.Z && horPaneFacing.getAxis() == EnumFacing.Axis.X;
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
        placementToIndexReverse = new EnumFacing[EnumFacing.VALUES.length][];
        for (EnumFacing placement : EnumFacing.VALUES){
            int p = placement.ordinal();
            placementToIndex[p] = new EnumFacing[4];
            placementToIndexReverse[p] = new EnumFacing[6];
            for (int i = 0; i < 4; i++) {
                EnumFacing realfacing = getFacingStuff(placement, i);
                placementToIndex[p][i] = realfacing;
                placementToIndexReverse[p][realfacing.ordinal()] = indexToFacing[i];
            }
        }
    }

    @Nonnull
    private static EnumFacing getFacingStuff(EnumFacing placement, int index){
        if (index > 3 || index < 0){
            throw new IllegalArgumentException();
        }
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
                throw new IllegalArgumentException();
        }
    }

}

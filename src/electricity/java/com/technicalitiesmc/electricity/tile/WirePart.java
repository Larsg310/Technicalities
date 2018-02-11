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
import java.util.function.Predicate;

/**
 * Created by Elec332 on 25-1-2018.
 */
public class WirePart {

    public WirePart(EnumFacing placement, int size){
        this.placement = placement;
        this.size = size;
        makeAABBMimicRenderLogic = false;
    }

    protected TileBundledElectricWire wire;
    private boolean makeAABBMimicRenderLogic;

    //Real data
    private int size = 1;
    private int colors = 0;
    private EnumFacing placement;
    public EnumBitSet<EnumFacing> realConnections = EnumBitSet.noneOf(EnumFacing.class);
    public NonNullList<EnumConnectionPlace> corners = NonNullList.withSize(EnumFacing.VALUES.length, EnumConnectionPlace.NORMAL);

    //Derived variables (for Client)
    public EnumBitSet<EnumFacing> connections = EnumBitSet.noneOf(EnumFacing.class), change = EnumBitSet.noneOf(EnumFacing.class);
    public BitSet extended = new BitSet(EnumFacing.VALUES.length);
    public BitSet shortened = new BitSet(EnumFacing.VALUES.length);

    private static final EnumFacing[] indexToFacing, st1, st2;
    public static final EnumFacing[][] placementToIndex, placementToIndexReverse;
    public static final int[] horFacingToIndex;

    public int getWireSize() {
        return size;
    }

    public EnumFacing getPlacement() {
        return placement;
    }

    public int getWireAmount(){
        return Integer.bitCount(colors);
    }

    public int getMaxWires(){
        return getMaxWires(size);
    }

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
        return ItemBundledWire.withCables(ColorHelper.getColors(getColorBits()), size);
    }

    public void checkConnections(BlockPos pos_, World world){
        System.out.println("checkConnections "+pos_);
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
                        if (oWp != null && oWp.size == size && JavaHelper.hasAtLeastOneMatch(ColorHelper.getColors(oWp.getColorBits()), ColorHelper.getColors(getColorBits()))) {
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
        if (wire != null) {
            wire.syncWireData();
        }
    }

    public void addBoxes(IBlockState state, World world, BlockPos pos, List<AxisAlignedBB> boxes, boolean hitbox, boolean onlySmallCenterBox) {
        addBoxes(boxes, false, connections, hitbox, onlySmallCenterBox);
    }

    private void addBoxes(List<AxisAlignedBB> boxes, boolean extend, Set<EnumFacing> connections, boolean hitbox, boolean onlySmallCenterBox){
        float width = ColorHelper.getColors(getColorBits()).size() * size;
        float stuff = ((16 - width) / 2) / 16;
        float stuff2 = .5f;
        float expansion = 0.1f / 16;
        if (connections.size() != 1 && !TileBundledElectricWire.isStraightLine(connections) || onlySmallCenterBox) {
            if (onlySmallCenterBox){
                width -= 2;
            }
            float ft = (16 - (width + 2)) / 32f;
            AxisAlignedBB blob = new AxisAlignedBB(ft, 0, ft, 1 - ft, 1/16f * size, 1 - ft);
            blob = blob.grow(expansion, 0, expansion);
            boxes.add(new IndexedAABB(AABBHelper.rotateFromDown(blob, placement), placement.ordinal()));
            stuff2 = stuff;
        }
        if (onlySmallCenterBox){
            return;
        }
        for (EnumFacing facing : connections){
            boolean z = facing.getAxis() == EnumFacing.Axis.Z;
            boolean n = facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;
            boolean ext = extend || (isExtendedHorizontal(facing) || (!makeAABBMimicRenderLogic && !hitbox)) && extended.get(facing.ordinal());
            boolean shrt = (makeAABBMimicRenderLogic || hitbox) && shortened.get(facing.ordinal()) && isExtendedHorizontal(facing);
            float eadd = ext ? 1/16f * size : (shrt ? -1/16f * size : 0);
            AxisAlignedBB aabb = new AxisAlignedBB(z ? stuff : 1 + eadd, 0, z ? 1 - stuff2 : stuff, 1 - (z ? stuff : stuff2), 1/16f * size, z ? 1 + eadd : 1 - stuff);
            if (n){
                float offset = -(1 - stuff2 + eadd);
                aabb = aabb.offset(z ? 0 : offset, 0, z ? offset : 0);
            }
            aabb = aabb.grow(expansion, 0, expansion);
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

    public boolean addWires(Pair<Integer, List<WireColor>> data){
        if (data.getLeft() != size){
            return false;
        }
        Set<WireColor> colors = Sets.newHashSet(data.getRight());
        if (colors.size() != data.getRight().size() || colors.size() + getWireAmount() > getMaxWires()){
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

    private boolean addWire(WireColor color) {
        WirePart copy = new WirePart(placement, size);
        copy.colors = colors;
        if (!copy.addWire_(color, false)){
            return false;
        }
        if (wire == null){
            addWire_(color, false);
            return true;
        }

        if (occludes(copy, wire.getPos(), Collections.emptySet(), wire.getWorld(), wire.getPos(), abl -> {
            for (AxisAlignedBB aabb : abl){
                if (!(aabb instanceof IndexedAABB && ((IndexedAABB) aabb).index == placement.ordinal())){
                    return true;
                }
            }
            return false;
        })) {
            return false;
        }

        addWire_(color, true);
        return true;
    }

    private boolean addWire_(WireColor color, boolean notify){
        if (hasWire(color)){
            return false;
        }
        if (Integer.bitCount(colors) >= getMaxWires(size)){
            return false;
        }
        colors = ColorHelper.addWire(color, colors);
        if (notify) {
            cS();
        }
        return true;
    }

    public static int getMaxWires(int size){
        switch (size){
            case 4:
                return 2;
            case 3:
                return 3;
            default:
                return 12 / size;
        }
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

    public boolean setColors(List<WireColor> colors){
        boolean ret = false;
        this.colors = 0;
        for (WireColor color : colors){
            ret |= !addWire_(color, false);
        }
        cS();
        return !ret;
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
                Set<EnumFacing> f = wire.connections.clone();
                f.add(placementToIndexReverse[wire.placement.ordinal()][to.ordinal()]);
                BlockPos pos = myPos.offset(to);
                if (occludes(wire, myPos, f, world, pos)){
                    return null;
                }
                return Pair.of(pos.offset(wire.placement), to.getOpposite());
            }

        };

        @Nullable
        public abstract Pair<BlockPos, EnumFacing> modify(World world, BlockPos myPos, WirePart wire, EnumFacing to);

    }

    public boolean isExtendedHorizontal(EnumFacing horPaneFacing){
        //return placement.getAxis() == EnumFacing.Axis.Y || placement.getAxis() == EnumFacing.Axis.Z && horPaneFacing.getAxis() == EnumFacing.Axis.X;
        return isExtendedReal(placementToIndex[placement.ordinal()][horFacingToIndex[horPaneFacing.ordinal()]]);
    }

    private boolean isExtendedReal(EnumFacing facing){
        //if (true){
        //    EnumFacing f = placementToIndexReverse[placement.ordinal()][facing.ordinal()];
        //    return isExtendedHorizontal(f);
        //}
        if (placement.getAxis() == EnumFacing.Axis.Y){
            boolean b = (placement.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE);
            return b == (facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE);
        }
        boolean plNeg = (placement.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) == (facing.getAxis() == EnumFacing.Axis.X);
        return plNeg != (facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE);
    }

    public static boolean occludes(WirePart wire, BlockPos wirePos, Set<EnumFacing> sides, World world, BlockPos otherPos){
        return occludes(wire, wirePos, sides, world, otherPos, axisAlignedBBS -> true);
    }

    public static boolean occludes(WirePart wire, BlockPos wirePos, Set<EnumFacing> sides, World world, BlockPos otherPos, Predicate<List<AxisAlignedBB>> occludeChecker){
        IBlockState state = WorldHelper.getBlockState(world, otherPos);
        List<AxisAlignedBB> abl = Lists.newArrayList(), abs = Lists.newArrayList();
        wire.addBoxes(abl, true, sides, true, true);
        for (AxisAlignedBB bb : abl) {
            abs.clear();
            int startIndex = 10;
            if (bb instanceof IndexedAABB){
                startIndex += ((IndexedAABB) bb).index;
            }
            bb = new IndexedAABB(bb, startIndex);
            state.addCollisionBoxToList(world, otherPos, bb.offset(wirePos), abs, null, false);
            if (!abs.isEmpty() && occludeChecker.test(abs)){
                return true;
            }
        }
        return false;
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
        horFacingToIndex = new int[EnumFacing.VALUES.length];
        Arrays.fill(horFacingToIndex, -1);
        for (EnumFacing placement : EnumFacing.VALUES){
            int p = placement.ordinal();
            placementToIndex[p] = new EnumFacing[4];
            placementToIndexReverse[p] = new EnumFacing[6];
            for (int i = 0; i < 4; i++) {
                EnumFacing realfacing = getFacingStuff(placement, i);
                placementToIndex[p][i] = realfacing;
                placementToIndexReverse[p][realfacing.ordinal()] = indexToFacing[i];
                if (placement == EnumFacing.NORTH){
                    horFacingToIndex[indexToFacing[i].ordinal()] = i;
                }
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

package com.technicalitiesmc.electricity.wires.ground;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.technicalitiesmc.electricity.item.ItemBundledWire;
import com.technicalitiesmc.electricity.tile.TileBundledElectricWire;
import com.technicalitiesmc.electricity.wires.WireColorHelper;
import com.technicalitiesmc.electricity.util.EnumBitSet;
import com.technicalitiesmc.electricity.wires.WireColor;
import com.technicalitiesmc.electricity.util.WireFacingHelper;
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

    public TileBundledElectricWire wire;
    private boolean makeAABBMimicRenderLogic;

    //Real data
    private int size = 1;
    private int colors = 0;
    private EnumFacing placement;
    public EnumBitSet<EnumFacing> realConnections = EnumBitSet.noneOf(EnumFacing.class);
    public NonNullList<EnumConnectionPlace> corners = NonNullList.withSize(EnumFacing.VALUES.length, EnumConnectionPlace.NORMAL);

    //Derived variables (for Client)
    public EnumBitSet<EnumFacing> connections, connectionsLast = connections = EnumBitSet.noneOf(EnumFacing.class), change, changeLast = change = EnumBitSet.noneOf(EnumFacing.class);
    public BitSet extended, extendedLast = extended = new BitSet(EnumFacing.VALUES.length);
    public BitSet shortened, shortenedLast = shortened = new BitSet(EnumFacing.VALUES.length);

    public WirePart(EnumFacing placement, int size) {
        this.placement = placement;
        this.size = size;
        makeAABBMimicRenderLogic = false;
    }

    public int getWireSize() {
        return size;
    }

    public EnumFacing getPlacement() {
        return placement;
    }

    public int getWireAmount() {
        return Integer.bitCount(colors);
    }

    public int getMaxWires() {
        return getMaxWires(size);
    }

    public int getColorBits() {
        return colors;
    }

    public boolean isStraightLine() {
        return WireFacingHelper.isStraightLine(connections);
    }

    public boolean canStay(World world, BlockPos pos) {
        return WorldHelper.getBlockState(world, pos.offset(placement)).isSideSolid(world, pos.offset(placement), placement.getOpposite());
    }

    public ItemStack getDropStack() {
        return ItemBundledWire.withCables(WireColorHelper.getColors(getColorBits()), size);
    }

    @Nonnull
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("wrclr", colors);
        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        this.colors = compound.getInteger("wrclr");
    }

    public void writeClientData(NBTTagCompound tag) {
        System.out.println("send " + wire.getPos());
        tag.setLong("conn", connections.getSerialized());
        tag.setLong("change", change.getSerialized());
        tag.setByteArray("extCon", extended.toByteArray());
        tag.setByteArray("shoCon", shortened.toByteArray());
    }

    @SideOnly(Side.CLIENT)
    public void readClientData(NBTTagCompound tag) {
        System.out.println("receiuve ");
        connections.deserialize(tag.getLong("conn"));
        change.deserialize(tag.getLong("change"));
        extended = BitSet.valueOf(tag.getByteArray("extCon"));
        shortened = BitSet.valueOf(tag.getByteArray("shoCon"));
    }

    public void checkConnections(BlockPos pos_, World world) {
        System.out.println("checkConnections " + pos_);
        moveToLastAndClear();
        for (int i = 0; i < 4; i++) {
            EnumFacing facing = WireFacingHelper.getRealSide(placement, i);
            for (EnumConnectionPlace cp : EnumConnectionPlace.values()) {
                Pair<BlockPos, EnumFacing> pbf = cp.modify(world, pos_, this, facing);
                if (pbf == null) {
                    continue;
                }
                BlockPos pos = pbf.getLeft();
                EnumFacing otherPlacement = pbf.getRight();
                if (WorldHelper.chunkLoaded(world, pos)) {
                    TileEntity tile = WorldHelper.getTileAt(world, pos);
                    if (tile instanceof TileBundledElectricWire) {
                        TileBundledElectricWire wireO = (TileBundledElectricWire) tile;
                        WirePart oWp = wireO.getWire(otherPlacement);
                        if (oWp != null && oWp.size == size && JavaHelper.hasAtLeastOneMatch(WireColorHelper.getColors(oWp.getColorBits()), WireColorHelper.getColors(getColorBits()))) {
                            EnumFacing iF = WireFacingHelper.getSideFromHorizontalIndex(i);
                            connections.add(iF);
                            if (oWp.colors != colors) {
                                change.add(iF);
                            }
                            realConnections.add(facing);
                            corners.set(facing.ordinal(), cp);
                            if (cp == EnumConnectionPlace.CORNER_DOWN) {
                                extended.set(iF.ordinal());
                            } else if (cp == EnumConnectionPlace.CORNER_UP) {
                                shortened.set(iF.ordinal());
                            }
                            break;
                        }
                    }
                }
            }
        }
        syncData();
    }

    private void moveToLastAndClear(){
        realConnections.clear();
        corners.clear();
        this.connectionsLast = connections;
        this.connections = EnumBitSet.noneOf(EnumFacing.class);
        this.changeLast = change;
        this.change = EnumBitSet.noneOf(EnumFacing.class);
        this.extendedLast = extended;
        this.extended = new BitSet(EnumFacing.VALUES.length);
        this.shortenedLast = shortened;
        this.shortened = new BitSet(EnumFacing.VALUES.length);
    }

    private void syncData(){
        if (wire != null && (
                !connectionsLast.equals(this.connections) ||
                !changeLast.equals(this.change) ||
                !extendedLast.equals(this.extended) ||
                !shortenedLast.equals(this.shortened)
        )) {
            wire.syncWireData();
        }
    }

    public void addBoxes(IBlockState state, World world, BlockPos pos, List<AxisAlignedBB> boxes, boolean hitbox, boolean onlySmallCenterBox) {
        addBoxes(boxes, false, connections, hitbox, onlySmallCenterBox);
    }

    private void addBoxes(List<AxisAlignedBB> boxes, boolean extend, Set<EnumFacing> connections, boolean hitbox, boolean onlySmallCenterBox) {
        float width = WireColorHelper.getColors(getColorBits()).size() * size;
        float stuff = ((16 - width) / 2) / 16;
        float stuff2 = .5f;
        float expansion = 0.1f / 16;
        if (connections.size() != 1 && !WireFacingHelper.isStraightLine(connections) || onlySmallCenterBox) {
            if (onlySmallCenterBox) {
                width -= 2;
            }
            float ft = (16 - (width + 2)) / 32f;
            AxisAlignedBB blob = new AxisAlignedBB(ft, 0, ft, 1 - ft, 1 / 16f * size, 1 - ft);
            blob = blob.grow(expansion, 0, expansion);
            boxes.add(new IndexedAABB(AABBHelper.rotateFromDown(blob, placement), placement.ordinal()));
            stuff2 = stuff;
        }
        if (onlySmallCenterBox) {
            return;
        }
        for (EnumFacing facing : connections) {
            boolean z = facing.getAxis() == EnumFacing.Axis.Z;
            boolean n = facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;
            boolean ext = extend || (isExtendedHorizontal(facing) || (!makeAABBMimicRenderLogic && !hitbox)) && extended.get(facing.ordinal());
            boolean shrt = (makeAABBMimicRenderLogic || hitbox) && shortened.get(facing.ordinal()) && isExtendedHorizontal(facing);
            float eadd = ext ? 1 / 16f * size : (shrt ? -1 / 16f * size : 0);
            AxisAlignedBB aabb = new AxisAlignedBB(z ? stuff : 1 + eadd, 0, z ? 1 - stuff2 : stuff, 1 - (z ? stuff : stuff2), 1 / 16f * size, z ? 1 + eadd : 1 - stuff);
            if (n) {
                float offset = -(1 - stuff2 + eadd);
                aabb = aabb.offset(z ? 0 : offset, 0, z ? offset : 0);
            }
            aabb = aabb.grow(expansion, 0, expansion);
            boxes.add(new IndexedAABB(AABBHelper.rotateFromDown(aabb, placement), placement.ordinal()));
        }
    }

    public boolean hasWire(WireColor color) {
        return WireColorHelper.hasWire(color, colors);
    }

    public boolean addWires(Pair<Integer, List<WireColor>> data) {
        if (data.getLeft() != size) {
            return false;
        }
        Set<WireColor> colors = Sets.newHashSet(data.getRight());
        if (colors.size() != data.getRight().size() || colors.size() + getWireAmount() > getMaxWires()) {
            return false;
        }
        for (WireColor color : colors) {
            if (hasWire(color)) {
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
        if (!copy.addWire_(color, false)) {
            return false;
        }
        if (wire == null) {
            addWire_(color, false);
            return true;
        }

        if (occludes(copy, wire.getPos(), Collections.emptySet(), wire.getWorld(), wire.getPos(), abl -> {
            for (AxisAlignedBB aabb : abl) {
                if (!(aabb instanceof IndexedAABB && ((IndexedAABB) aabb).index == placement.ordinal())) {
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

    private boolean addWire_(WireColor color, boolean notify) {
        if (hasWire(color)) {
            return false;
        }
        if (Integer.bitCount(colors) >= getMaxWires(size)) {
            return false;
        }
        colors = WireColorHelper.addWire(color, colors);
        if (notify) {
            cS();
        }
        return true;
    }

    private void cS() {
        if (wire != null) {
            wire.notifyNeighborsOfChangeExtensively();
            wire.syncWireData();
        }
    }

    public boolean removeWire(WireColor color) {
        if (hasWire(color)) {
            colors = WireColorHelper.removeWire(color, colors);
            cS();
            return true;
        }
        return false;
    }

    public boolean setColors(List<WireColor> colors) {
        boolean ret = false;
        this.colors = 0;
        for (WireColor color : colors) {
            ret |= !addWire_(color, false);
        }
        cS();
        return !ret;
    }

    public boolean isExtendedHorizontal(EnumFacing horPaneFacing) {
        return isExtendedReal(WireFacingHelper.getRealSide(placement, horPaneFacing));
    }

    private boolean isExtendedReal(EnumFacing facing) {
        return WireFacingHelper.isCheckSide(placement, facing);
    }

    public static boolean occludes(WirePart wire, BlockPos wirePos, Set<EnumFacing> sides, World world, BlockPos otherPos) {
        return occludes(wire, wirePos, sides, world, otherPos, axisAlignedBBS -> true);
    }

    public static boolean occludes(WirePart wire, BlockPos wirePos, Set<EnumFacing> sides, World world, BlockPos otherPos, Predicate<List<AxisAlignedBB>> occludeChecker) {
        IBlockState state = WorldHelper.getBlockState(world, otherPos);
        List<AxisAlignedBB> abl = Lists.newArrayList(), abs = Lists.newArrayList();
        wire.addBoxes(abl, true, sides, true, true);
        for (AxisAlignedBB bb : abl) {
            abs.clear();
            int startIndex = 10;
            if (bb instanceof IndexedAABB) {
                startIndex += ((IndexedAABB) bb).index;
            }
            bb = new IndexedAABB(bb, startIndex);
            state.addCollisionBoxToList(world, otherPos, bb.offset(wirePos), abs, null, false);
            if (!abs.isEmpty() && occludeChecker.test(abs)) {
                return true;
            }
        }
        return false;
    }

    public static int getMaxWires(int size) {
        switch (size) {
            case 4:
                return 2;
            case 3:
                return 3;
            default:
                return 12 / size;
        }
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
                f.add(WireFacingHelper.getHorizontalFacingFromReal(wire.placement, to));
                BlockPos pos = myPos.offset(to);
                if (occludes(wire, myPos, f, world, pos)) {
                    return null;
                }
                return Pair.of(pos.offset(wire.placement), to.getOpposite());
            }

        };

        @Nullable
        public abstract Pair<BlockPos, EnumFacing> modify(World world, BlockPos myPos, WirePart wire, EnumFacing to);

    }

}

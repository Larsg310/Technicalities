package com.technicalitiesmc.electricity.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.technicalitiesmc.electricity.TKElectricity;
import com.technicalitiesmc.electricity.item.ItemBundledWire;
import com.technicalitiesmc.electricity.tile.TileBundledElectricWire;
import com.technicalitiesmc.electricity.wires.ground.WirePart;
import com.technicalitiesmc.electricity.wires.WireColor;
import com.technicalitiesmc.lib.IndexedAABB;
import com.technicalitiesmc.lib.RayTraceHelper;
import com.technicalitiesmc.lib.block.BlockBase;
import elec332.core.util.ItemStackHelper;
import elec332.core.util.PlayerHelper;
import elec332.core.util.UniversalUnlistedProperty;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by Elec332 on 19-1-2018.
 */
public class BlockBundledElectricWire extends BlockBase implements ITileEntityProvider {

    public static final IUnlistedProperty<RenderData> PROPERTY_RENDERDATA = new UniversalUnlistedProperty<>("tkerenderdata", RenderData.class);

    public BlockBundledElectricWire() {
        super(Material.CIRCUITS);
        setCreativeTab(TKElectricity.creativeTab);
    }

    public static RenderData fromItem(@Nonnull ItemStack stack) {
        return new RenderData(ItemBundledWire.getColorsFromStack(stack));
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
        return new TileBundledElectricWire();
    }

    @Override
    protected boolean isFull(IBlockState state) {
        return false;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, @Nonnull BlockPos pos) {
        return true;//worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos, EnumFacing.UP);
    }

    @Override
    public void addSelectionBoxes(IBlockState state, World world, BlockPos pos, List<AxisAlignedBB> boxes, RayTraceResult hit) {
        TileBundledElectricWire tile = getTile(world, pos, TileBundledElectricWire.class);
        if (hit != null) {
            WirePart wire = tile.getWire(EnumFacing.VALUES[hit.subHit]);
            if (wire != null) {
                wire.addBoxes(state, world, pos, boxes, false, false);
            }
        } else {
            for (WirePart wirePart : tile.getWireView()) {
                wirePart.addBoxes(state, world, pos, boxes, false, false);
            }
        }
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean p_185477_7_) {
        entityBox = entityBox.offset(new BlockPos(0, 0, 0).subtract(pos));
        List<AxisAlignedBB> list = Lists.newArrayList();
        TileBundledElectricWire tile = getTile(world, pos, TileBundledElectricWire.class);
        for (WirePart wirePart : tile.getWireView()) {
            wirePart.addBoxes(state, world, pos, list, true, entityBox instanceof IndexedAABB && ((IndexedAABB) entityBox).index > 9);
        }
        for (AxisAlignedBB box : list) {
            if (box.intersects(entityBox)) {
                collidingBoxes.add(box.offset(pos));
            }
        }
    }

    @Override
    @Nonnull
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    @Nonnull
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(PROPERTY_RENDERDATA).build();
    }

    @Override
    @Nonnull
    public IBlockState getExtendedState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) {
        IExtendedBlockState iebs = (IExtendedBlockState) state;
        return iebs.withProperty(PROPERTY_RENDERDATA, new RenderData(getTile(world, pos, TileBundledElectricWire.class)));
    }

    @Override
    public void getSubBlocksC(@Nonnull Item item, List<ItemStack> subBlocks, CreativeTabs creativeTab) {
        for (int i = 1; i < 5; i++) { //Add all 4 wire sizes, RIP creative tab
            for (WireColor color : WireColor.values()) {
                subBlocks.add(ItemBundledWire.withCables(i, color));
            }
        }
    }

    @Override
    public boolean removedByPlayer(@Nonnull IBlockState state, World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player, boolean willHarvest) {
        if (world.isRemote) {
            return false;
        }
        TileBundledElectricWire tile = getTile(world, pos, TileBundledElectricWire.class);
        Pair<Vec3d, Vec3d> vec = RayTraceHelper.getRayTraceVectors(player);
        RayTraceResult hit = collisionRayTrace(state, world, pos, vec.getLeft(), vec.getRight());
        if (hit != null) {
            WirePart wirePart = tile.getWire(EnumFacing.VALUES[hit.subHit]);
            if (wirePart != null) {
                tile.remove(wirePart);
                //if (!tile.getWireView().isEmpty()) {
                tile.notifyNeighborsOfChangeExtensively();
                //}
                ItemStack stack = wirePart.getDropStack();
                spawnAsEntity(world, pos, stack);
            }
        }
        return false;
    }

    @Override
    public void neighborChangedC(World world, BlockPos pos, IBlockState state, Block neighbor, BlockPos p_189540_5_) {
        neighborChanged(world, pos, p_189540_5_, false);
    }

    @Override
    public void observedNeighborChange(IBlockState observerState, World world, BlockPos observerPos, Block changedBlock, BlockPos changedBlockPos) {
        neighborChanged(world, observerPos, changedBlockPos, true);
    }

    private void neighborChanged(World world, BlockPos pos, BlockPos neighborPos, boolean observer) {
        if (!world.isRemote) {
            TileBundledElectricWire tile = getTile(world, pos, TileBundledElectricWire.class);
            if (observer && !tile.shouldRefresh(world.getTotalWorldTime(), neighborPos)) {
                return;
            }
            List<WirePart> wp = Lists.newArrayList();
            for (WirePart p : tile.getWireView()) {
                if (!p.canStay(world, pos)) {
                    spawnAsEntity(world, pos, p.getDropStack());
                    wp.add(p);
                }
            }
            tile.removeAll(wp);
            if (wp.isEmpty()) {
                tile.checkConnections();
            }
        }
    }

    @Override
    public void getDrops(@Nonnull NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, @Nonnull IBlockState state, int fortune) {
    }

    @Override
    public boolean onBlockActivatedC(World world, BlockPos pos, EntityPlayer player, EnumHand hand, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ) {
        WirePart wp = getTile(world, pos, TileBundledElectricWire.class).getWire(facing.getOpposite());
        if (wp != null && hand == EnumHand.MAIN_HAND && !world.isRemote) {
            List<String> info = Lists.newArrayList();
            for (EnumFacing facing1 : EnumFacing.VALUES) {
                if (wp.realConnections.contains(facing1)) {
                    info.add(facing1 + "  " + wp.corners.get(facing1.ordinal()));
                }
            }
            PlayerHelper.sendMessageToPlayer(player, wp.getWireSize() + "  " + info.toString());
        }
        return super.onBlockActivatedC(world, pos, player, hand, state, facing, hitX, hitY, hitZ);
    }

    @Override
    @Nonnull
    @SuppressWarnings("deprecation")
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    @Nonnull
    public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, EntityPlayer player) {
        TileBundledElectricWire tile = getTile(world, pos, TileBundledElectricWire.class);
        WirePart wire = tile.getWire(EnumFacing.VALUES[target.subHit]);
        if (wire != null) {
            return wire.getDropStack();
        }
        return ItemStackHelper.NULL_STACK;
    }

    public static class RenderData {

        private final List<WirePart> wires;
        private final boolean item;

        private RenderData(TileBundledElectricWire tile) {
            this.wires = ImmutableList.copyOf(tile.getWireView());
            this.item = false;
        }
        private RenderData(Pair<Integer, List<WireColor>> data) {
            wires = Lists.newArrayList();
            WirePart wire = new WirePart(EnumFacing.DOWN, data.getLeft());
            wires.add(wire);
            wire.connections.add(EnumFacing.NORTH);
            wire.connections.add(EnumFacing.SOUTH);
            wire.setColors(data.getRight());
            this.item = true;
        }

        public List<WirePart> getWires() {
            return wires;
        }

        public boolean isItem() {
            return item;
        }
/*
        @Override
        public boolean equals(Object obj) {
            return obj instanceof RenderData && equals((RenderData) obj);
        }

        private boolean equals(RenderData renderData){
            return sides.equals(renderData.sides) && colors.equals(renderData.colors) && wirechange.equals(renderData.wirechange) && (colors.size() != 1 || item == renderData.item) ;
        }

        @Override
        public int hashCode() {
            return colors.hashCode() + sides.hashCode() * 76 + wirechange.hashCode() * 34 + 49 * (colors.size() == 1 ? (item ? 1 : 3) : 0);
        }*/

    }

}

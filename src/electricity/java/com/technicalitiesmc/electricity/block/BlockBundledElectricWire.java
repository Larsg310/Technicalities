package com.technicalitiesmc.electricity.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.technicalitiesmc.electricity.TKElectricity;
import com.technicalitiesmc.electricity.item.ItemBundledWire;
import com.technicalitiesmc.electricity.tile.TileBundledElectricWire;
import com.technicalitiesmc.electricity.tile.WirePart;
import com.technicalitiesmc.electricity.util.ColorHelper;
import com.technicalitiesmc.electricity.util.EnumBitSet;
import com.technicalitiesmc.electricity.util.WireColor;
import com.technicalitiesmc.lib.IndexedAABB;
import com.technicalitiesmc.lib.block.BlockBase;
import elec332.core.api.client.IColoredBlock;
import elec332.core.util.UniversalUnlistedProperty;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created by Elec332 on 19-1-2018.
 */
public class BlockBundledElectricWire extends BlockBase implements ITileEntityProvider, IColoredBlock {

    public static final IUnlistedProperty<RenderData> PROPERTY_RENDERDATA = new UniversalUnlistedProperty<>("tkerenderdata", RenderData.class);

    public BlockBundledElectricWire() {
        super(Material.CIRCUITS);
        setCreativeTab(TKElectricity.creativeTab);
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
        return worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos, EnumFacing.UP);
    }

    @Override
    public void addBoxes(IBlockState state, World world, BlockPos pos, List<AxisAlignedBB> boxes) {
        TileBundledElectricWire wire = getTile(world, pos, TileBundledElectricWire.class);
        for (WirePart wirePart : wire.wires){
            List<AxisAlignedBB> boxez = Lists.newArrayList();
            wirePart.addBoxes(state, world, pos, boxez);
            boxes.forEach(axisAlignedBB -> boxes.add(new IndexedAABB(axisAlignedBB, wirePart.placement.ordinal())));
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
        for (WireColor color : WireColor.values()){
            subBlocks.add(ItemBundledWire.withCables(color));
        }
    }

    @Override
    public void neighborChangedC(World world, BlockPos pos, IBlockState state, Block neighbor, BlockPos p_189540_5_) {
        if (!world.isRemote) {
            TileBundledElectricWire tile = getTile(world, pos, TileBundledElectricWire.class);
            List<WirePart> wp = Lists.newArrayList();
            for (WirePart p : tile.wires){
                if (!p.canStay(world, pos)){
                    spawnAsEntity(world, pos, p.getDropStack());
                    wp.add(p);
                }
            }
            tile.wires.removeAll(wp);
            if (tile.wires.isEmpty()){
                world.setBlockToAir(pos);
            }
        }
    }

    @Override
    public void getDrops(@Nonnull NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, @Nonnull IBlockState state, int fortune) {
        //drops.add(ItemBundledWire.withCables(ColorHelper.getColors(getTile(world, pos, TileBundledElectricWire.class).getColorBits())));
    }

    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {

        //getTile(world, pos, TileBundledElectricWire.class).checkConnections(true);
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
        TileBundledElectricWire tile = getTile(world, pos, TileBundledElectricWire.class); //todo: wire from RTR
        return ItemBundledWire.withCables(ColorHelper.getColors(tile.wires.get(0).getColorBits()));
    }

    public static RenderData fromItem(@Nonnull ItemStack stack){
        return new RenderData(ItemBundledWire.getColorsFromStack(stack));
    }

    public static class RenderData {

        private RenderData(TileBundledElectricWire tile){
            this.wires = ImmutableList.copyOf(tile.wires);
            this.item = false;
        }

        private RenderData(List<WireColor> colors){
            wires = Lists.newArrayList();
            WirePart wire = new WirePart(EnumFacing.DOWN);
            wires.add(new WirePart(EnumFacing.DOWN));
            wire.connections.add(EnumFacing.NORTH);
            wire.connections.add(EnumFacing.SOUTH);
            wire.setColors(colors);
            this.item = true;
        }

        private final List<WirePart> wires;
        private final boolean item;

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

package com.technicalitiesmc.electricity.block;

import com.technicalitiesmc.electricity.TKElectricity;
import com.technicalitiesmc.electricity.item.ItemBundledWire;
import com.technicalitiesmc.electricity.tile.TileBundledElectricWire;
import com.technicalitiesmc.electricity.util.ColorHelper;
import com.technicalitiesmc.lib.block.BlockBase;
import elec332.core.api.client.IColoredBlock;
import elec332.core.util.UniversalUnlistedProperty;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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
    public void addBoxes(IBlockState state, World world, BlockPos pos, List<AxisAlignedBB> boxes) {
        TileBundledElectricWire wire = getTile(world, pos, TileBundledElectricWire.class);
        float width = ColorHelper.getColors(wire.getColorBits()).size();
        float stuff = ((16 - width) / 2) / 16;
        float stuff2 = .5f;
        if (wire.connections.size() != 1) {
            float ft = stuff;
            if (!wire.isStraightLine()) {
                ft = (16 - (width + 2)) / 32f;
            }
            boxes.add(new AxisAlignedBB(ft, 0, ft, 1 - ft, 1 / 16f, 1 - ft));
            stuff2 = stuff;
        }
        for (EnumFacing facing : wire.connections){
            boolean z = facing.getAxis() == EnumFacing.Axis.Z;
            boolean n = facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;
            AxisAlignedBB aabb = new AxisAlignedBB(z ? stuff : 1, 0, z ? 1 - stuff2 : stuff, 1 - (z ? stuff : stuff2), 1/16f, z ? 1 : 1 - stuff);
            if (n){
                float offset = -(1 - stuff2);
                aabb = aabb.offset(z ? 0 : offset, 0, z ? offset : 0);
            }
            boxes.add(aabb);
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
        for (EnumDyeColor color : EnumDyeColor.values()){
            subBlocks.add(ItemBundledWire.withCables(color));
        }
    }

    @Override
    @Nonnull
    public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, EntityPlayer player) {
        TileBundledElectricWire tile = getTile(world, pos, TileBundledElectricWire.class);
        return ItemBundledWire.withCables(ColorHelper.getColors(tile.getColorBits()));
    }

    public static RenderData fromItem(@Nonnull ItemStack stack){
        return new RenderData(ItemBundledWire.getColorsFromStack(stack));
    }

    public static class RenderData {

        private RenderData(TileBundledElectricWire tile){
            this.sides = tile.connections;
            this.straightLine = tile.isStraightLine();
            this.colors = ColorHelper.getColors(tile.getColorBits());
            this.wirechange = tile.change;
        }

        private RenderData(List<EnumDyeColor> colors){
            this.sides = EnumSet.of(EnumFacing.NORTH, EnumFacing.SOUTH);
            this.straightLine = true;
            this.colors = colors;
            this.wirechange = EnumSet.noneOf(EnumFacing.class);
        }

        private final EnumSet<EnumFacing> sides;
        private final boolean straightLine;
        private final List<EnumDyeColor> colors;
        private final Set<EnumFacing> wirechange;

        public List<EnumDyeColor> getColors() {
            return colors;
        }

        public boolean isStraightLine() {
            return straightLine;
        }

        public EnumSet<EnumFacing> getSides() {
            return sides;
        }

        public Set<EnumFacing> getWirechange() {
            return wirechange;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof RenderData && equals((RenderData) obj);
        }

        private boolean equals(RenderData renderData){
            return sides.equals(renderData.sides) && colors.equals(renderData.colors) && wirechange.equals(renderData.wirechange);
        }

        @Override
        public int hashCode() {
            return colors.hashCode() + sides.hashCode() * 76 + wirechange.hashCode() * 34;
        }

    }

}

package com.technicalitiesmc.base.block;

import com.technicalitiesmc.api.heat.IThermalMaterial;
import com.technicalitiesmc.base.Technicalities;
import com.technicalitiesmc.base.init.TKBaseItems;
import com.technicalitiesmc.base.item.ItemWrench;
import com.technicalitiesmc.base.tile.TileHeatPipe;
import com.technicalitiesmc.lib.block.BlockFloatingPipeBase;
import com.technicalitiesmc.lib.block.TileFloatingPipeBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockHeatPipe extends BlockFloatingPipeBase {
    private final float width;

    public BlockHeatPipe(float width) {
        super(width, Material.IRON);
        this.width = width;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, RayTraceResult hit) {
        if (hit == null) return false;
        Item item = player.getHeldItem(hand).item;

        if (item == TKBaseItems.wrench) {
            EnumFacing side = hit.subHit >= 0 && hit.subHit < 6
                ? EnumFacing.getFront(hit.subHit)
                : hit.sideHit;

            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileHeatPipe) {
                TileHeatPipe heatPipe = (TileHeatPipe) te;
                heatPipe.toggleSide(side);

                TileEntity connectionTile = world.getTileEntity(pos.offset(side));
                if (connectionTile instanceof TileHeatPipe && connectionTile.getBlockType() == heatPipe.getBlockType()) {
                    ((TileHeatPipe) connectionTile).toggleSide(side.getOpposite());
                }
                ItemWrench.playWrenchSound(world, pos);
                return true;
            }
        }

        return false;
    }

    @Nullable
    @Override
    public TileFloatingPipeBase createNewTileEntity(@Nonnull World worldIn, int meta) {
        return new TileHeatPipe();
    }

    public enum ThermalMaterial implements IThermalMaterial {
        SMALL(4 / 16f),
        MEDIUM(6 / 16f),
        LARGE(8 / 16f);

        ThermalMaterial(float width) {
            centerVolume = width * width * width;
            sideVolume = (1 - width) / 2 * width * width;
        }

        private final float centerVolume;
        private final float sideVolume;
        private final ResourceLocation resourceLocation = new ResourceLocation(Technicalities.MODID, "heat_pipe_" + name().toLowerCase());

        @Override
        public double getSpecificHeatCapacity() {
            return 390.0;
        }

        @Override
        public double getThermalConductivity() {
            return 401.0;
        }

        @Override
        public double getDensity() {
            return 8960.0;
        }

        @Override
        public double getM3(IBlockState state) {
            return centerVolume + sideVolume * BlockHeatPipe.CONNECTIONS.stream().filter(state::getValue).count();
        }

        @Override
        public boolean conductsHeat(IBlockState state) {
            return true;
        }

        @Nonnull
        @Override
        public ResourceLocation getRegistryName() {
            return resourceLocation;
        }
    }
}

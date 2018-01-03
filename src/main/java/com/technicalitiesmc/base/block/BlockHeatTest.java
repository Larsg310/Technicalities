package com.technicalitiesmc.base.block;

import com.technicalitiesmc.base.tile.TileHeatTest;
import com.technicalitiesmc.lib.block.BlockBase;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Elec332 on 3-1-2018.
 */
public class BlockHeatTest extends BlockBase implements ITileEntityProvider {

    public BlockHeatTest(Material material) {
        super(material);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
        return new TileHeatTest();
    }

}

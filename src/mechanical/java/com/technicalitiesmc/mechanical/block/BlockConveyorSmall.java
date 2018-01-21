package com.technicalitiesmc.mechanical.block;

import com.technicalitiesmc.mechanical.tile.TileConveyorSmall;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockConveyorSmall extends BlockConveyorBase {
    public BlockConveyorSmall() {
        super(9 / 16f);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileConveyorSmall();
    }
}

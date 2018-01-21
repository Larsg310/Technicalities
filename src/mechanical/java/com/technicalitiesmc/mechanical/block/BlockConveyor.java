package com.technicalitiesmc.mechanical.block;

import com.technicalitiesmc.mechanical.tile.TileConveyor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockConveyor extends BlockConveyorBase {
    public BlockConveyor() {
        super(1f);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileConveyor();
    }
}

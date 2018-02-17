package com.technicalitiesmc.mechanical.block;

import com.technicalitiesmc.lib.block.BlockBase;
import com.technicalitiesmc.mechanical.tile.TileFlywheel;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class BlockFlywheel extends BlockBase implements ITileEntityProvider {
    public static final AxisAlignedBB BASE_BOUNDS = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.25, 1.0);
    public static final AxisAlignedBB SHAFT_BOUNDS = new AxisAlignedBB(6 / 16.0, 0.0, 6 / 16.0, 10 / 16.0, 1.0, 10 / 16.0);

    public BlockFlywheel() {
        super(Material.ROCK);
        setSoundType(SoundType.STONE);
        setHardness(1.5f);
    }

    @Override
    public boolean onBlockActivatedC(World world, BlockPos pos, EntityPlayer player, EnumHand hand, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        boolean result = Optional.ofNullable(world.getTileEntity(pos))
            .filter(it -> it instanceof TileFlywheel)
            .map(it -> (TileFlywheel) it)
            .map(it -> it.tryAddItem(stack))
            .orElse(false);
        if (result && !player.isCreative()) stack.shrink(1);
        return result;
    }

    @Override
    public boolean removedByPlayer(@Nonnull IBlockState state, World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player, boolean willHarvest) {
        Optional.ofNullable(world.getTileEntity(pos))
            .filter(it -> it instanceof TileFlywheel)
            .map(it -> (TileFlywheel) it)
            .ifPresent(TileFlywheel::dropDisks);
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    protected boolean isFull(IBlockState state) {
        return false;
    }

    @Override
    public void addBoxes(IBlockState state, World world, BlockPos pos, List<AxisAlignedBB> boxes) {
        boxes.add(BASE_BOUNDS);
        boxes.add(SHAFT_BOUNDS);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileFlywheel();
    }
}

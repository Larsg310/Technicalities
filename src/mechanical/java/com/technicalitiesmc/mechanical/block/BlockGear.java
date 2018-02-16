package com.technicalitiesmc.mechanical.block;

import com.technicalitiesmc.lib.block.BlockBase;
import com.technicalitiesmc.mechanical.tile.TileGear;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.BiFunction;

public class BlockGear extends BlockBase implements ITileEntityProvider {

    private static final AxisAlignedBB[] BOXES = new AxisAlignedBB[]{
        new AxisAlignedBB(0, 0, 0, 1, 2 / 16F, 1),
        new AxisAlignedBB(0, 14 / 16F, 0, 1, 1, 1),
        new AxisAlignedBB(0, 0, 0, 1, 1, 2 / 16F),
        new AxisAlignedBB(0, 0, 14 / 16F, 1, 1, 1),
        new AxisAlignedBB(0, 0, 0, 2 / 16F, 1, 1),
        new AxisAlignedBB(14 / 16F, 0, 0, 1, 1, 1)
    };

    public BlockGear() {
        super(Material.WOOD);
        setDefaultState(getDefaultState().withProperty(BlockDirectional.FACING, EnumFacing.DOWN));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileGear();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(BlockDirectional.FACING).build();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BlockDirectional.FACING, EnumFacing.VALUES[meta]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BlockDirectional.FACING).ordinal();
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
        super.onEntityCollidedWithBlock(world, pos, state, entity);
        float velocity = Optional.ofNullable(world.getTileEntity(pos))
            .filter(it -> it instanceof TileGear)
            .map(it -> ((TileGear) it).getVelocity())
            .orElse(0f);

        float cosTheta = MathHelper.cos((float) Math.toRadians(-velocity));
        float sinTheta = MathHelper.sin((float) Math.toRadians(-velocity));

        entity.rotationYaw -= velocity;
        BiFunction<Float, Float, Float> fx = (x, y) -> x * cosTheta + y * -sinTheta;
        BiFunction<Float, Float, Float> fy = (x, y) -> x * sinTheta + y * cosTheta;
        float xoff = pos.getX() + 0.5f;
        float yoff = pos.getZ() + 0.5f;
        float relX = (float) (entity.posX - xoff);
        float relY = (float) (entity.posZ - yoff);
        float newX = fx.apply(relX, relY);
        float newY = fy.apply(relX, relY);
        entity.setPosition(newX + xoff, entity.posY, newY + yoff);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOXES[state.getValue(BlockDirectional.FACING).ordinal()];
    }

    @Override
    protected boolean isFull(IBlockState state) {
        return false;
    }


    @Nonnull
    @Override
    public IBlockState getBlockStateForPlacementC(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, @Nullable EnumHand hand) {
        return getDefaultState().withProperty(BlockDirectional.FACING, EnumFacing.DOWN);//facing.getOpposite());
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

}

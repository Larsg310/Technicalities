package com.technicalitiesmc.lib.block;

import com.technicalitiesmc.lib.IndexedAABB;
import com.technicalitiesmc.lib.funcint.LambdaUtils;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// It's dangerous to have duplicate code; take this!
// - Farfetch'd
public abstract class BlockFloatingPipeBase extends BlockBase implements ITileEntityProvider {
    public static final List<PropertyBool> CONNECTIONS = Arrays.asList(
        PropertyBool.create("down"),
        PropertyBool.create("up"),
        PropertyBool.create("north"),
        PropertyBool.create("south"),
        PropertyBool.create("west"),
        PropertyBool.create("east")
    );

    public final List<AxisAlignedBB> BOXES;

    public BlockFloatingPipeBase(float width, Material material) {
        super(material);
        setHardness(0.5f);

        float sideDist = (1 - width) / 2;
        this.BOXES = Arrays.asList(
            new IndexedAABB(sideDist, 0, sideDist, 1 - sideDist, sideDist, 1 - sideDist, 0),
            new IndexedAABB(sideDist, 1 - sideDist, sideDist, 1 - sideDist, 1, 1 - sideDist, 1),
            new IndexedAABB(sideDist, sideDist, 0, 1 - sideDist, 1 - sideDist, sideDist, 2),
            new IndexedAABB(sideDist, sideDist, 1 - sideDist, 1 - sideDist, 1 - sideDist, 1, 3),
            new IndexedAABB(0, sideDist, sideDist, sideDist, 1 - sideDist, 1 - sideDist, 4),
            new IndexedAABB(1 - sideDist, sideDist, sideDist, 1, 1 - sideDist, 1 - sideDist, 5),
            new IndexedAABB(sideDist, sideDist, sideDist, 1 - sideDist, 1 - sideDist, 1 - sideDist, 6)
        );
    }

    @Nullable
    @Override
    public abstract TileFloatingPipeBase createNewTileEntity(@Nonnull World worldIn, int meta);

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(CONNECTIONS.toArray(new PropertyBool[0])).build();
    }

    @Override
    protected boolean isFull(IBlockState state) {
        return false;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        IBlockState actualState = super.getActualState(state, worldIn, pos);
        return Optional.ofNullable(worldIn.getTileEntity(pos))
            .map(LambdaUtils.cast(TileFloatingPipeBase.class))
            .map(te -> {
                IBlockState actualState1 = actualState;
                for (int i = 0; i < 6; i++)
                    actualState1 = actualState1.withProperty(CONNECTIONS.get(i), te.isConnected(EnumFacing.getFront(i)));
                return actualState1;
            })
            .orElse(actualState);
    }

    @Nonnull
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOXES.get(6);
    }

    @Override
    public void addBoxes(IBlockState state, World world, BlockPos pos, List<AxisAlignedBB> boxes) {
        boxes.add(BOXES.get(6));
        Optional.ofNullable(world.getTileEntity(pos))
            .map(LambdaUtils.cast(TileFloatingPipeBase.class))
            .ifPresent(it -> boxes.addAll(IntStream.range(0, 6)
                .filter(i -> it.isConnected(EnumFacing.getFront(i)))
                .mapToObj(BOXES::get)
                .collect(Collectors.toSet())));
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        if (!worldIn.isRemote) {
            Optional.ofNullable(worldIn.getTileEntity(pos))
                .map(LambdaUtils.cast(TileFloatingPipeBase.class))
                .ifPresent(TileFloatingPipeBase::updateConnections);
        }
    }

    @Override
    public void neighborChangedC(World world, BlockPos pos, IBlockState state, Block neighbor, BlockPos neighborPos) {
        super.neighborChangedC(world, pos, state, neighbor, neighborPos);
        if (!world.isRemote) {
            Optional.ofNullable(world.getTileEntity(pos))
                .map(LambdaUtils.cast(TileFloatingPipeBase.class))
                .ifPresent(TileFloatingPipeBase::updateConnections);
        }
    }
}

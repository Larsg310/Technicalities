package com.technicalitiesmc.base.block;

import com.technicalitiesmc.base.tile.TileValve;
import com.technicalitiesmc.lib.block.BlockBase;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class BlockValve extends BlockBase implements ITileEntityProvider {

    private static final int MAX_REACH = 14;

    public static final IProperty<EnumValveMode> MODE = PropertyEnum.create("mode", EnumValveMode.class);

    public BlockValve() {
        super(Material.IRON);
        setDefaultState(getDefaultState().withProperty(MODE, EnumValveMode.OUT));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileValve();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(MODE).build();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(MODE).ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(MODE, EnumValveMode.VALUES[meta]);
    }

    @Override
    public IBlockState getBlockStateForPlacementC(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta,
                                                  EntityLivingBase placer, @Nullable EnumHand hand) {
        return getDefaultState();
    }

    @Override
    public boolean onBlockActivatedC(World world, BlockPos pos, EntityPlayer player, EnumHand hand, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) {
            world.setBlockState(pos, state.cycleProperty(MODE));
            return true;
        }

        BlockPos start = pos.offset(facing.getOpposite());
        state = world.getBlockState(start);
        if (!isValidInside(world, start, state)) {
            return false; // NOPE!
        }

        Pair<BlockPos, BlockPos> bounds = findInnerBounds(world, start);
        Map<BlockPos, EnumFacing.AxisDirection> valves = new HashMap<>();
        if (!validateTank(world, bounds.getLeft(), bounds.getRight(), valves)) {
            return false; // NOPE!
        }

        // Success!
        Map<FluidStack, Integer> fluids = new HashMap<>();
        valves.forEach((p, dir) -> {
            TileValve valve = getTile(world, p);
            TileValve.TankInterface itf = valve.getInterface(dir);
            for (FluidStack stack : itf.getFluids()) {
                stack = stack.copy();
                int amt = stack.amount;
                stack.amount = 1;
                fluids.put(stack, amt);
            }
        });
        int total = fluids.values().stream().mapToInt(i -> i).sum();

        Vec3i size = bounds.getRight().subtract(bounds.getLeft());
        if (total > 8000 * size.getX() * size.getY() * size.getZ()) {
            return false; // NOPE!
        }

        return true;
    }

    private Pair<BlockPos, BlockPos> findInnerBounds(World world, BlockPos start) {
        int[] size = new int[3];
        BlockPos minPos = start, maxPos = start;
        for (EnumFacing f : EnumFacing.VALUES) {
            int max = MAX_REACH - size[f.getAxis().ordinal()];
            for (int i = 1; i < max; i++) {
                BlockPos p = start.offset(f, i);
                IBlockState state = world.getBlockState(p);
                if (!isValidInside(world, p, state)) {
                    size[f.getAxis().ordinal()] += i - 1;
                    if (i > 1) {
                        p = start.offset(f, i - 1);
                        if (f.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
                            minPos = new BlockPos(Math.min(minPos.getX(), p.getX()), Math.min(minPos.getY(), p.getY()), Math.min(minPos.getZ(), p.getZ()));
                        } else {
                            maxPos = new BlockPos(Math.max(maxPos.getX(), p.getX()), Math.max(maxPos.getY(), p.getY()), Math.max(maxPos.getZ(), p.getZ()));
                        }
                    }
                    break;
                }
            }
        }
        return Pair.of(minPos, maxPos);
    }

    private boolean validateTank(World world, BlockPos min, BlockPos max, Map<BlockPos, EnumFacing.AxisDirection> valves) {
        for (BlockPos pos : BlockPos.getAllInBoxMutable(min.add(-1, -1, -1), max.add(1, 1, 1))) {
            IBlockState state = world.getBlockState(pos);
            boolean isX = pos.getX() == min.getX() - 1 || pos.getX() == max.getX() + 1;
            boolean isY = pos.getY() == min.getY() - 1 || pos.getY() == max.getY() + 1;
            boolean isZ = pos.getZ() == min.getZ() - 1 || pos.getZ() == max.getZ() + 1;
            if (isX || isY || isZ) { // Outside
                if ((isX && isY) || (isY && isZ) || (isZ && isX)) { // Edge
                    if (!isValidEdge(world, pos, state)) {
                        return false;
                    }
                } else if (!isValidWall(world, pos, state)) {
                    return false;
                } else { // Valid wall
                    if (state.getBlock() == this) {
                        valves.put(pos.toImmutable(), isX && pos.getX() > max.getX() || isY && pos.getY() > max.getY() || isZ && pos.getZ() > max.getZ()
                                ? EnumFacing.AxisDirection.POSITIVE : EnumFacing.AxisDirection.NEGATIVE);
                    }
                }
            } else if (!isValidInside(world, pos, state)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidEdge(World world, BlockPos pos, IBlockState state) {
        return state.getBlock().isNormalCube(state, world, pos) && state.getBlock() != this;
    }

    private boolean isValidWall(World world, BlockPos pos, IBlockState state) {
        return !state.getBlock().isAir(state, world, pos) && !state.getBlock().isReplaceable(world, pos); // TODO: Look into this. There needs to be a better way...
    }

    private boolean isValidInside(World world, BlockPos pos, IBlockState state) {
        return state.getBlock().isAir(state, world, pos);
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED;
    }

    public enum EnumValveMode implements IStringSerializable {
        IN, OUT, NONE;
        public static final EnumValveMode[] VALUES = values();

        @Override
        public String getName() {
            return name().toLowerCase();
        }

    }

}

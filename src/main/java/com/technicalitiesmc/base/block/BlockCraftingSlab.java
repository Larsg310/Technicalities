package com.technicalitiesmc.base.block;

import com.technicalitiesmc.base.tile.TileCraftingSlab;
import com.technicalitiesmc.lib.block.BlockBase;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class BlockCraftingSlab extends BlockBase implements ITileEntityProvider {

    public static final IProperty<EnumFacing> PROPERTY_FACING = PropertyEnum.create("front", EnumFacing.class, EnumFacing.HORIZONTALS);
    public static final IProperty<Boolean> PROPERTY_LOCKED = PropertyBool.create("locked");

    private static final AxisAlignedBB BOX = new AxisAlignedBB(0, 0, 0, 1, 6 / 16D, 1);

    public BlockCraftingSlab() {
        super(Material.ROCK);
        setHardness(3F);
        setDefaultState(getDefaultState().withProperty(PROPERTY_LOCKED, false));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileCraftingSlab();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(PROPERTY_FACING, PROPERTY_LOCKED).build();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(PROPERTY_FACING).getHorizontalIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(PROPERTY_FACING, EnumFacing.getHorizontal(meta));
    }

    @Nonnull
    @Override
    public IBlockState getBlockStateForPlacementC(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, @Nullable EnumHand hand) {
        return getDefaultState().withProperty(PROPERTY_FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileCraftingSlab tile = ((TileCraftingSlab) world.getTileEntity(pos));
        return state.withProperty(PROPERTY_LOCKED, tile.isLocked());
    }

    @Override
    protected boolean isFull(IBlockState state) {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOX;
    }

    @Override
    public boolean onBlockActivatedC(World world, BlockPos pos, EntityPlayer player, EnumHand hand, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ) {
        int x = hitX > 4 / 16F && hitX < 6 / 16F ? 0 : hitX > 7 / 16F && hitX < 9 / 16F ? 1 : hitX > 10 / 16F && hitX < 12 / 16F ? 2 : -1;
        int z = hitZ > 4 / 16F && hitZ < 6 / 16F ? 0 : hitZ > 7 / 16F && hitZ < 9 / 16F ? 1 : hitZ > 10 / 16F && hitZ < 12 / 16F ? 2 : -1;

        TileCraftingSlab tile = ((TileCraftingSlab) world.getTileEntity(pos));

        if (x == -1 || z == -1) {
            if (!tile.isLocked() && player.isSneaking()) {
                if (!world.isRemote) {
                    tile.lock();
                }
                return true;
            }
            return false;
        }

        if (tile.isLocked()) {
            return false;
        }

        switch (state.getValue(PROPERTY_FACING)) {
            case NORTH:
                x = 2 - x;
                z = 2 - z;
                break;
            case SOUTH:
                // NO-OP
                break;
            case WEST:
                int v1 = 2 - x;
                x = z;
                z = v1;
                break;
            case EAST:
                int v2 = 2 - z;
                z = x;
                x = v2;
                break;
            default:
                break;
        }

        if (tile.getStack(x, z).isEmpty()) {
            ItemStack stack = player.getHeldItem(hand).copy();
            if (!stack.isEmpty()) {
                if (!world.isRemote) {
                    stack.setCount(1);
                    tile.setStack(x, z, stack);
                }
                return true;
            }
        } else {
            if (!world.isRemote) {
                tile.setStack(x, z, ItemStack.EMPTY);
            }
            return true;
        }

        return false;
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
        if (world.isRemote) {
            return;
        }

        TileCraftingSlab tile = ((TileCraftingSlab) world.getTileEntity(pos));
        if (tile.isLocked()) {
            ItemStack stack = tile.retrieveOutput();
            if (!stack.isEmpty()) {
                ItemHandlerHelper.giveItemToPlayer(player, stack);
            }
        }
    }

    @Override
    protected boolean canBreak(World world, BlockPos pos, EntityPlayer player) {
        TileCraftingSlab tile = ((TileCraftingSlab) world.getTileEntity(pos));
        return !tile.isLocked() || player.isSneaking();
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileCraftingSlab tile = (TileCraftingSlab) world.getTileEntity(pos);
        if (tile.isLocked()) {
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 3; y++) {
                    InventoryHelper.spawnItemStack(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, tile.getGridItem(x, y));
                }
            }
        }
        ItemStack stack = ItemStack.EMPTY;
        while (!(stack = tile.retrieveOutput()).isEmpty()) {
            InventoryHelper.spawnItemStack(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        }
        world.updateComparatorOutputLevel(pos, this);

        super.breakBlock(world, pos, state);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        TileCraftingSlab tile = ((TileCraftingSlab) world.getTileEntity(pos));
        tile.update();
    }

}

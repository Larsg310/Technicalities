package com.technicalitiesmc.base.block;

import com.technicalitiesmc.base.tile.TileCrate;
import com.technicalitiesmc.util.block.BlockBase;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockCrate extends BlockBase implements ITileEntityProvider {

    public BlockCrate() {
        super(Material.WOOD);
        setHardness(3.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileCrate();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing,
            float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (!stack.isEmpty()) {
            TileCrate tile = (TileCrate) world.getTileEntity(pos);
            ItemStack leftover = ItemHandlerHelper.insertItem(tile.getInventory(), stack, world.isRemote);
            if (!world.isRemote) {
                player.setHeldItem(hand, leftover);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
        if (!world.isRemote) {
            TileCrate tile = (TileCrate) world.getTileEntity(pos);
            IItemHandler inv = tile.getInventory();
            ItemStack extracted = ItemStack.EMPTY;
            for (int i = 0; i < inv.getSlots(); i++) {
                extracted = inv.extractItem(!player.isSneaking() ? inv.getSlots() - i - 1 : i, 64, false);
                if (!extracted.isEmpty()) {
                    break;
                }
            }
            if (!extracted.isEmpty()) {
                ItemHandlerHelper.giveItemToPlayer(player, extracted);
            }
        }
    }

    @Override
    protected boolean canBreak(World world, BlockPos pos, EntityPlayer player) {
        if (player.isSneaking() || !player.capabilities.isCreativeMode) {
            return true;
        }

        TileCrate tile = (TileCrate) world.getTileEntity(pos);
        if (tile.getInventory().isEmpty()) {
            return true;
        }

        return false;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileCrate tile = (TileCrate) world.getTileEntity(pos);
        IItemHandler inv = tile.getInventory();
        for (int i = 0; i < inv.getSlots(); i++) {
            InventoryHelper.spawnItemStack(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, inv.getStackInSlot(i));
        }
        world.updateComparatorOutputLevel(pos, this);

        super.breakBlock(world, pos, state);
    }

    @Override
    protected boolean isFull(IBlockState state) {
        return true;
    }

}

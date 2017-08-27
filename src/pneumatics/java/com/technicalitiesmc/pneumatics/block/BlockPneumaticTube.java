package com.technicalitiesmc.pneumatics.block;

import java.util.List;
import java.util.Random;

import com.technicalitiesmc.api.pneumatics.TubeModule;
import com.technicalitiesmc.pneumatics.init.TKPneumaticsItems;
import com.technicalitiesmc.pneumatics.tile.TilePneumaticTubeBase;
import com.technicalitiesmc.pneumatics.tile.TilePneumaticTubeClient;
import com.technicalitiesmc.pneumatics.tile.TilePneumaticTubeServer;
import com.technicalitiesmc.pneumatics.tube.module.ModuleManager;
import com.technicalitiesmc.util.IndexedAABB;
import com.technicalitiesmc.util.UnlistedProperty;
import com.technicalitiesmc.util.block.BlockBase;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockPneumaticTube extends BlockBase implements ITileEntityProvider {

    @SuppressWarnings("rawtypes")
    public static final IUnlistedProperty[] CONNECTIONS = new IUnlistedProperty[] { //
            UnlistedProperty.ofType(Connection.class, "down"), //
            UnlistedProperty.ofType(Connection.class, "up"), //
            UnlistedProperty.ofType(Connection.class, "north"), //
            UnlistedProperty.ofType(Connection.class, "south"), //
            UnlistedProperty.ofType(Connection.class, "west"), //
            UnlistedProperty.ofType(Connection.class, "east") //
    };

    private static final double SIDE_DIST = 0.25;
    public static final AxisAlignedBB[] BOXES = new AxisAlignedBB[] { //
            new IndexedAABB(SIDE_DIST, 0, SIDE_DIST, 1 - SIDE_DIST, SIDE_DIST, 1 - SIDE_DIST, 0), //
            new IndexedAABB(SIDE_DIST, 1 - SIDE_DIST, SIDE_DIST, 1 - SIDE_DIST, 1, 1 - SIDE_DIST, 1), //
            new IndexedAABB(SIDE_DIST, SIDE_DIST, 0, 1 - SIDE_DIST, 1 - SIDE_DIST, SIDE_DIST, 2), //
            new IndexedAABB(SIDE_DIST, SIDE_DIST, 1 - SIDE_DIST, 1 - SIDE_DIST, 1 - SIDE_DIST, 1, 3), //
            new IndexedAABB(0, SIDE_DIST, SIDE_DIST, SIDE_DIST, 1 - SIDE_DIST, 1 - SIDE_DIST, 4), //
            new IndexedAABB(1 - SIDE_DIST, SIDE_DIST, SIDE_DIST, 1, 1 - SIDE_DIST, 1 - SIDE_DIST, 5), //
            new AxisAlignedBB(SIDE_DIST, SIDE_DIST, SIDE_DIST, 1 - SIDE_DIST, 1 - SIDE_DIST, 1 - SIDE_DIST) //
    };

    public BlockPneumaticTube() {
        super(Material.PISTON);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return world.isRemote ? new TilePneumaticTubeClient() : new TilePneumaticTubeServer();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(CONNECTIONS).build();
    }

    @Override
    protected boolean isFull(IBlockState state) {
        return false;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @SuppressWarnings("unchecked")
    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TilePneumaticTubeBase)) {
            return state;
        }
        TilePneumaticTubeBase tube = (TilePneumaticTubeBase) te;
        IExtendedBlockState ebs = (IExtendedBlockState) state;
        for (EnumFacing face : EnumFacing.VALUES) {
            ebs = ebs.withProperty(CONNECTIONS[face.ordinal()], new Connection(tube, face));
        }
        return ebs;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOXES[6];
    }

    @Override
    public void addBoxes(IBlockState state, World world, BlockPos pos, List<AxisAlignedBB> boxes) {
        boxes.add(BOXES[6]);

        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TilePneumaticTubeBase)) {
            return;
        }
        TilePneumaticTubeBase tube = (TilePneumaticTubeBase) te;

        for (int i = 0; i < 6; i++) {
            if (tube.isConnected(EnumFacing.getFront(i))) {
                boxes.add(BOXES[i]);
            }
        }
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos, RayTraceResult hit) {
        if (hit == null || hit.subHit < 0) {
            return super.getSelectedBoundingBox(state, world, pos, hit);
        }
        return BOXES[hit.subHit];
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, RayTraceResult hit) {
        if (hit == null) {
            return false;
        }
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TilePneumaticTubeBase)) {
            return false;
        }
        TilePneumaticTubeBase tube = (TilePneumaticTubeBase) te;

        EnumFacing sideHit = hit.subHit == -1 ? hit.sideHit : EnumFacing.getFront(hit.subHit);

        TubeModule mod = tube.getModule(sideHit);
        if (mod != null) {
            return mod.onActivated(player, hand);
        } else {
            ItemStack stack = player.getHeldItem(hand);
            if (stack.getItem() == TKPneumaticsItems.tube_module) {
                TubeModule.Type<?> type = ModuleManager.INSTANCE.get(stack.getMetadata());
                if (type != null) {
                    return tube.setModule(sideHit, type);
                }
            }
        }

        return false;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TilePneumaticTubeServer)) {
            return;
        }
        TilePneumaticTubeServer tube = (TilePneumaticTubeServer) te;

        if (pos.equals(fromPos)) {
            tube.computeNeighbor(null);
        } else {
            BlockPos dif = fromPos.subtract(pos);
            EnumFacing dir = EnumFacing.getFacingFromVector(dif.getX(), dif.getY(), dif.getZ());
            if (pos.offset(dir).equals(fromPos)) {
                tube.computeNeighbor(dir);
            }
        }
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TilePneumaticTubeServer) {
            ((TilePneumaticTubeServer) te).computeNeighbor(null);
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TilePneumaticTubeServer) {
            ((TilePneumaticTubeServer) te).computeNeighbor(null);
        }
    }

    public class Connection {

        public final boolean connected;
        public final TubeModule module;

        public Connection(TilePneumaticTubeBase tube, EnumFacing side) {
            this.connected = tube.isConnected(side);
            this.module = tube.getModule(side);
        }

    }

}

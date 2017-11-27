package com.technicalitiesmc.lib.block;

import com.technicalitiesmc.base.Technicalities;
import com.technicalitiesmc.lib.IndexedAABB;
import elec332.core.inventory.window.IWindowHandler;
import elec332.core.inventory.window.WindowManager;
import elec332.core.tile.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class BlockBase extends AbstractBlock {

    public BlockBase(Material material) {
        super(material);
    }

    private String unlName;

    protected String createUnlocalizedName(){
        return "tile." + getRegistryName().toString().replace(":", ".").toLowerCase();
    }

    @Nonnull
    @Override
    public Block setSoundType(@Nonnull SoundType sound) {
        return super.setSoundType(sound);
    }

    @Nonnull
    @Override
    public String getUnlocalizedName() {
        if (this.unlName == null){
            unlName = createUnlocalizedName();
        }
        return unlName;
    }

    public void addBoxes(IBlockState state, World world, BlockPos pos, List<AxisAlignedBB> boxes) {
        boxes.add(state.getBoundingBox(world, pos));
    }

    public void addSelectionBoxes(IBlockState state, World world, BlockPos pos, List<AxisAlignedBB> boxes) {
        addBoxes(state, world, pos, boxes);
    }

    public void addCollisionBoxes(IBlockState state, World world, BlockPos pos, List<AxisAlignedBB> boxes) {
        addBoxes(state, world, pos, boxes);
    }

    @Override
    @SuppressWarnings("deprecation")
    public RayTraceResult collisionRayTrace(IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
        List<AxisAlignedBB> boxes = new ArrayList<>();
        addSelectionBoxes(state, world, pos, boxes);
        return boxes.stream().reduce(null, (prev, box) -> {
            RayTraceResult hit = rayTrace(pos, start, end, box);
            if (hit != null && box instanceof IndexedAABB) {
                hit.subHit = ((IndexedAABB) box).index;
            }
            return prev != null && (hit == null || start.squareDistanceTo(prev.hitVec) < start.squareDistanceTo(hit.hitVec)) ? prev : hit;
        }, (a, b) -> b);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void addCollisionBoxToList(IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean p_185477_7_) {
        entityBox = entityBox.offset(new BlockPos(0, 0, 0).subtract(pos));
        List<AxisAlignedBB> list = new ArrayList<>();
        addCollisionBoxes(state, world, pos, list);
        for (AxisAlignedBB box : list) {
            if (box.intersects(entityBox)) {
                collidingBoxes.add(box.offset(pos));
            }
        }
    }

    @Deprecated
    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, @Nonnull World world, @Nonnull BlockPos pos) {
        return getSelectedBoundingBox(state, world, pos, rayTrace(state, world, pos, Minecraft.getMinecraft().player)).offset(pos);
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos, RayTraceResult hit) {
        List<AxisAlignedBB> list = new ArrayList<>();
        addSelectionBoxes(state, world, pos, list);
        if (!list.isEmpty()) {
            AxisAlignedBB aabb = null;
            for (AxisAlignedBB box : list) {
                if (aabb == null) {
                    aabb = box;
                } else {
                    aabb = aabb.union(box);
                }
            }
            return aabb;
        }
        return state.getBoundingBox(world, pos);
    }

    @Deprecated
    @Override
    public boolean onBlockActivatedC(World world, BlockPos pos, EntityPlayer player, EnumHand hand, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ) {
        RayTraceResult hit = rayTrace(state, world, pos, player);
        return hit != null && onBlockActivated(world, pos, state, player, hand, hit);
    }

    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, RayTraceResult hit) {
        return super.onBlockActivated(world, pos, state, player, hand, hit.sideHit, (float) hit.hitVec.x - pos.getX(),
                (float) hit.hitVec.y - pos.getY(), (float) hit.hitVec.z - pos.getZ());
    }

    protected boolean isFull(IBlockState state) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullBlock(IBlockState state) {
        return isFull(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube(IBlockState state) {
        return isFull(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isNormalCube(IBlockState state) {
        return isFull(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(IBlockState state) {
        return isFull(state);
    }

    protected boolean canBreak(World world, BlockPos pos, EntityPlayer player) {
        return true;
    }

    @Override
    public boolean removedByPlayer(@Nonnull IBlockState state, World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player, boolean willHarvest) {
        if (!canBreak(world, pos, player)) {
            if (player.capabilities.isCreativeMode) {
                onBlockClicked(world, pos, player);
            }
            return false;
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    protected final RayTraceResult rayTrace(IBlockState state, World world, BlockPos pos, EntityPlayer entity) {
        float partialTicks = 0;
        double reach = world.isRemote ? getClientReach() : ((EntityPlayerMP) entity).interactionManager.getBlockReachDistance();
        Vec3d start = entity.getPositionEyes(partialTicks);
        Vec3d look = entity.getLook(partialTicks);
        Vec3d end = start.add(look.scale(reach));
        return collisionRayTrace(state, world, pos, start, end);
    }

    @SideOnly(Side.CLIENT)
    private double getClientReach() {
        return Minecraft.getMinecraft().playerController.getBlockReachDistance();
    }

    public boolean openTileWindow(EntityPlayer player, World world, BlockPos pos){
        return openWindow(player, Technicalities.proxy, world, pos, -1);
    }

    public boolean openWindow(EntityPlayer player, IWindowHandler windowHandler, World world, BlockPos pos, int id){
        WindowManager.openWindow(player, windowHandler, world, pos, (byte) id);
        return true;
    }

}

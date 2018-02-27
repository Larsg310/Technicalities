package com.technicalitiesmc.mechanical.block;

import com.technicalitiesmc.lib.block.BlockBase;
import com.technicalitiesmc.mechanical.tile.TileShaft;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.minecraft.client.renderer.GlStateManager.*;

public class BlockShaft extends BlockBase implements ITileEntityProvider {

    private static final AxisAlignedBB[] BOXES = new AxisAlignedBB[]{ //
            new AxisAlignedBB(0, 6 / 16D, 6 / 16D, 1, 10 / 16D, 10 / 16D), //
            new AxisAlignedBB(6 / 16D, 0, 6 / 16D, 10 / 16D, 1, 10 / 16D), //
            new AxisAlignedBB(6 / 16D, 6 / 16D, 0, 10 / 16D, 10 / 16D, 1)//
    };

    public BlockShaft() {
        super(Material.WOOD);
        setDefaultState(getDefaultState().withProperty(BlockRotatedPillar.AXIS, EnumFacing.Axis.Y));

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileShaft();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(BlockRotatedPillar.AXIS).build();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BlockRotatedPillar.AXIS, EnumFacing.Axis.values()[meta]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BlockRotatedPillar.AXIS).ordinal();
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOXES[state.getValue(BlockRotatedPillar.AXIS).ordinal()];
    }

    @Override
    protected boolean isFull(IBlockState state) {
        return false;
    }


    @Nonnull
    @Override
    public IBlockState getBlockStateForPlacementC(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, @Nullable EnumHand hand) {
        return getDefaultState().withProperty(BlockRotatedPillar.AXIS, facing.getAxis());
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @SubscribeEvent
    public void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
        RayTraceResult hit = event.getTarget();
        if (hit == null) return;
        if (hit.typeOfHit != RayTraceResult.Type.BLOCK) return;
        EntityPlayer player = event.getPlayer();
        IBlockState state = player.world.getBlockState(hit.getBlockPos());
        if (state.getBlock() != this) return;
        TileShaft te = getTile(player.world, hit.getBlockPos());

        pushMatrix();
        enableBlend();
        tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        glLineWidth(2.0F);
        disableTexture2D();
        depthMask(false);

        float partialTicks = event.getPartialTicks();
        double offX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks - hit.getBlockPos().getX();
        double offY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks - hit.getBlockPos().getY();
        double offZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks - hit.getBlockPos().getZ();

        translate(0.5 - offX, 0.5 - offY, 0.5 - offZ);
        Vec3i axis = EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, te.getRotationAxis()).getDirectionVec();
        rotate(te.getAngle(partialTicks), axis.getX(), axis.getY(), axis.getZ());
        translate(-0.5, -0.5, -0.5);
        RenderGlobal.drawSelectionBoundingBox(BOXES[state.getValue(BlockRotatedPillar.AXIS).ordinal()].grow(0.002), 0.0F, 0.0F, 0.0F, 0.4F);

        depthMask(true);
        enableTexture2D();
        disableBlend();
        popMatrix();

        event.setCanceled(true);
    }

}

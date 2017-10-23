package com.technicalitiesmc.base.client.render;

import static net.minecraft.client.renderer.GlStateManager.*;

import java.util.List;

import com.technicalitiesmc.base.tile.TileCraftingSlab;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager.CullFace;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemBed;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.pipeline.LightUtil;

public class TESRCraftingSlab extends TileEntitySpecialRenderer<TileCraftingSlab> {

    @Override
    public void render(TileCraftingSlab te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        pushMatrix();
        translate(x, y, z);

        translate(0.5, 0.5, 0.5);
        rotate(90 * (4 - te.getBlockMetadata()), 0, 1, 0);
        translate(-0.5, -0.5, -0.5);

        translate(5 / 16D, 5.5 / 16D, 5 / 16D);

        for (int x_ = 0; x_ < 3; x_++) {
            for (int y_ = 0; y_ < 3; y_++) {
                ItemStack stack = te.getStack(x_, y_);
                if (stack.isEmpty()) {
                    continue;
                }

                pushMatrix();

                if (renderFlat(stack)) {
                    translate(3 * x_ / 16D - 1 / 128D, 3.5 / 128D, 3 * y_ / 16D - 1 / 128D);
                    scale(0.125, 0.125, 0.125);

                    rotate(180, 0, 1, 0);
                    rotate(90, 1, 0, 0);
                    translate(-1 / 16D, -1 / 16D, -1 / 16D);
                } else {
                    translate(3 * x_ / 16D, 3 / 32D, 3 * y_ / 16D);
                    scale(0.25, 0.25, 0.25);
                    rotate(180, 0, 1, 0);
                }

                renderStack(stack, TransformType.FIXED, te.isStackPresent(x_, y_) ? alpha : 0.75F * alpha);

                popMatrix();
            }
        }

        popMatrix();

        pushMatrix();
        translate(x + 0.5, y + 0.75 + Math.sin(System.currentTimeMillis() / 500D) / 48D, z + 0.5);
        rotate((float) ((System.currentTimeMillis() / 25D) % 360), 0, 1, 0);

        ItemStack stack = te.getResult();
        if (renderFlat(stack)) {
            scale(0.5, 0.5, 0.5);
        } else {
            scale(0.75, 0.75, 0.75);
        }
        if (shouldRotateResult(stack)) {
            rotate(90, 1, 0, 0);
        }
        renderStack(stack, TransformType.FIXED, te.hasResult() ? alpha : 0.75F * alpha);

        popMatrix();
    }

    private void renderStack(ItemStack stack, TransformType transform, float alpha) {
        if (stack.isEmpty()) {
            return;
        }

        IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, null, null);

        rendererDispatcher.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        rendererDispatcher.renderEngine.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        color(1.0F, 1.0F, 1.0F, 1.0F);
        enableRescaleNormal();
        alphaFunc(516, 0.1F);
        enableBlend();
        tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        pushMatrix();
        model = ForgeHooksClient.handleCameraTransforms(model, transform, false);

        translate(-0.5F, -0.5F, -0.5F);
        if (model.isBuiltInRenderer()) {
            pushMatrix();
            color(1.0F, 1.0F, 1.0F, 1.0F);
            enableRescaleNormal();
            TileEntityItemStackRenderer.instance.renderByItem(stack, alpha);
            popMatrix();
        } else {
            int color = 0xFFFFFF | ((int) (alpha * 255) << 24);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(7, DefaultVertexFormats.ITEM);

            for (EnumFacing enumfacing : EnumFacing.values()) {
                renderQuads(bufferbuilder, model.getQuads((IBlockState) null, enumfacing, 0L), color, stack);
            }

            renderQuads(bufferbuilder, model.getQuads((IBlockState) null, (EnumFacing) null, 0L), color, stack);
            tessellator.draw();
        }

        cullFace(CullFace.BACK);
        popMatrix();
        disableRescaleNormal();
        disableBlend();
        rendererDispatcher.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        rendererDispatcher.renderEngine.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
    }

    private void renderQuads(BufferBuilder renderer, List<BakedQuad> quads, int color, ItemStack stack) {
        boolean flag = color == -1 && !stack.isEmpty();
        int i = 0;
        for (int j = quads.size(); i < j; ++i) {
            BakedQuad bakedquad = quads.get(i);
            int k = color;
            if (flag && bakedquad.hasTintIndex()) {
                k = Minecraft.getMinecraft().getItemColors().colorMultiplier(stack, bakedquad.getTintIndex());
                if (EntityRenderer.anaglyphEnable) {
                    k = TextureUtil.anaglyphColor(k);
                }
                k = k | 0xFEFFFFFF;
            }
            LightUtil.renderQuadColor(renderer, bakedquad, k);
        }
    }

    private boolean renderFlat(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemBlock)) {
            return true;
        }
        Block block = Block.getBlockFromItem(stack.getItem());
        return block instanceof BlockPane || block instanceof BlockRailBase;
    }

    private boolean shouldRotateResult(ItemStack stack) {
        return stack.getItem() instanceof ItemBed;
    }

}

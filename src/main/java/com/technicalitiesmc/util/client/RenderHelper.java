package com.technicalitiesmc.util.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager.*;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.lwjgl.opengl.GL11;

import java.util.List;

import static net.minecraft.client.renderer.GlStateManager.*;

public class RenderHelper {

    public static void renderStack(ItemStack stack, TransformType transform, float alpha) {
        if (stack.isEmpty()) {
            return;
        }

        IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, null, null);

        TextureManager engine = Minecraft.getMinecraft().renderEngine;

        engine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        engine.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
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
        engine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        engine.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
    }

    public static void renderQuads(BufferBuilder renderer, List<BakedQuad> quads, int color, ItemStack stack) {
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

    public static void renderCuboid(AxisAlignedBB aabb) {
        renderCuboid(aabb, Tessellator.getInstance().getBuffer());
        Tessellator.getInstance().draw();
    }

    public static void renderCuboid(AxisAlignedBB aabb, BufferBuilder buffer) {
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);

        // Bottom
        buffer.pos(aabb.minX, aabb.minY, aabb.minZ).tex(0, 0).normal(0, -1, 0).endVertex();
        buffer.pos(aabb.maxX, aabb.minY, aabb.minZ).tex(1, 0).normal(0, -1, 0).endVertex();
        buffer.pos(aabb.maxX, aabb.minY, aabb.maxZ).tex(1, 1).normal(0, -1, 0).endVertex();
        buffer.pos(aabb.minX, aabb.minY, aabb.maxZ).tex(0, 1).normal(0, -1, 0).endVertex();

        // Top
        buffer.pos(aabb.minX, aabb.maxY, aabb.minZ).tex(0, 0).normal(0, 1, 0).endVertex();
        buffer.pos(aabb.minX, aabb.maxY, aabb.maxZ).tex(0, 1).normal(0, 1, 0).endVertex();
        buffer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).tex(1, 1).normal(0, 1, 0).endVertex();
        buffer.pos(aabb.maxX, aabb.maxY, aabb.minZ).tex(1, 0).normal(0, 1, 0).endVertex();

        // North
        buffer.pos(aabb.minX, aabb.minY, aabb.minZ).tex(0, 0).normal(0, 0, -1).endVertex();
        buffer.pos(aabb.minX, aabb.maxY, aabb.minZ).tex(0, 1).normal(0, 0, -1).endVertex();
        buffer.pos(aabb.maxX, aabb.maxY, aabb.minZ).tex(1, 1).normal(0, 0, -1).endVertex();
        buffer.pos(aabb.maxX, aabb.minY, aabb.minZ).tex(1, 0).normal(0, 0, -1).endVertex();

        // South
        buffer.pos(aabb.minX, aabb.minY, aabb.maxZ).tex(0, 0).normal(0, 0, 1).endVertex();
        buffer.pos(aabb.maxX, aabb.minY, aabb.maxZ).tex(1, 0).normal(0, 0, 1).endVertex();
        buffer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).tex(1, 1).normal(0, 0, 1).endVertex();
        buffer.pos(aabb.minX, aabb.maxY, aabb.maxZ).tex(0, 1).normal(0, 0, 1).endVertex();

        // West
        buffer.pos(aabb.minX, aabb.minY, aabb.minZ).tex(0, 0).normal(-1, 0, 0).endVertex();
        buffer.pos(aabb.minX, aabb.minY, aabb.maxZ).tex(0, 1).normal(-1, 0, 0).endVertex();
        buffer.pos(aabb.minX, aabb.maxY, aabb.maxZ).tex(1, 1).normal(-1, 0, 0).endVertex();
        buffer.pos(aabb.minX, aabb.maxY, aabb.minZ).tex(1, 0).normal(-1, 0, 0).endVertex();

        // East
        buffer.pos(aabb.maxX, aabb.minY, aabb.minZ).tex(0, 0).normal(1, 0, 0).endVertex();
        buffer.pos(aabb.maxX, aabb.maxY, aabb.minZ).tex(1, 0).normal(1, 0, 0).endVertex();
        buffer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).tex(1, 1).normal(1, 0, 0).endVertex();
        buffer.pos(aabb.maxX, aabb.minY, aabb.maxZ).tex(0, 1).normal(1, 0, 0).endVertex();

    }

}

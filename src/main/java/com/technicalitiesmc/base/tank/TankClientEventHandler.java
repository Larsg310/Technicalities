package com.technicalitiesmc.base.tank;

import com.google.common.util.concurrent.AtomicDouble;
import com.technicalitiesmc.base.Technicalities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.Map;

@Mod.EventBusSubscriber(modid = Technicalities.MODID)
public class TankClientEventHandler {

    @SubscribeEvent
    public static void onRenderWorld(RenderWorldLastEvent event) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        } else {
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }

        GlStateManager.disableBlend();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        renderFluid(buffer, BlockRenderLayer.SOLID);
        tessellator.draw();

        GlStateManager.enableBlend();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        renderFluid(buffer, BlockRenderLayer.TRANSLUCENT);
        buffer.sortVertexData(
                (float) TileEntityRendererDispatcher.staticPlayerX,
                (float) TileEntityRendererDispatcher.staticPlayerY,
                (float) TileEntityRendererDispatcher.staticPlayerZ);
        tessellator.draw();

        buffer.setTranslation(0, 0, 0);

        RenderHelper.enableStandardItemLighting();
    }

    private static void renderFluid(BufferBuilder buffer, BlockRenderLayer layer) {
        AtomicDouble cumulativeHeight = new AtomicDouble(0);
        for (MultiblockTank tank : MultiblockTank.getAllTanks()) {
            BlockPos pos = tank.getCorner();
            Vec3i size = tank.getDimensions();
            float yMultiplier = (float) size.getY() / tank.getCapacity();

            buffer.setTranslation(
                    pos.getX() - TileEntityRendererDispatcher.staticPlayerX,
                    pos.getY() - TileEntityRendererDispatcher.staticPlayerY,
                    pos.getZ() - TileEntityRendererDispatcher.staticPlayerZ);

            Map<FluidStack, Integer> fluids = tank.getFluids();
            for (BlockPos p : BlockPos.getAllInBox(0, 0, 0, size.getX() - 1, 0, size.getZ() - 1)) {
                cumulativeHeight.set(0);
                fluids.forEach((stack, amt) -> {
                    float height = amt * yMultiplier;
                    float yPos = (float) cumulativeHeight.getAndAdd(height);

                    if (stack.getFluid().getBlock().canRenderInLayer(stack.getFluid().getBlock().getDefaultState(), layer)) {
                        ResourceLocation texturePath = stack.getFluid().getStill(stack);
                        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().registerSprite(texturePath);

                        // Top
                        buffer.pos(p.getX(), p.getY() + yPos + height, p.getZ()).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
                        buffer.pos(p.getX(), p.getY() + yPos + height, p.getZ() + 1).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
                        buffer.pos(p.getX() + 1, p.getY() + yPos + height, p.getZ() + 1).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
                        buffer.pos(p.getX() + 1, p.getY() + yPos + height, p.getZ()).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();

                        if (p.getX() == 0) {
                            buffer.pos(p.getX(), p.getY() + yPos, p.getZ()).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
                            buffer.pos(p.getX(), p.getY() + yPos, p.getZ() + 1).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
                            buffer.pos(p.getX(), p.getY() + yPos + height, p.getZ() + 1).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
                            buffer.pos(p.getX(), p.getY() + yPos + height, p.getZ()).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
                        }
                        if (p.getX() == size.getX() - 1) {
                            buffer.pos(p.getX() + 1, p.getY() + yPos, p.getZ()).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
                            buffer.pos(p.getX() + 1, p.getY() + yPos + height, p.getZ()).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
                            buffer.pos(p.getX() + 1, p.getY() + yPos + height, p.getZ() + 1).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
                            buffer.pos(p.getX() + 1, p.getY() + yPos, p.getZ() + 1).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
                        }
                        if (p.getZ() == 0) {
                            buffer.pos(p.getX(), p.getY() + yPos, p.getZ()).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
                            buffer.pos(p.getX(), p.getY() + yPos + height, p.getZ()).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
                            buffer.pos(p.getX() + 1, p.getY() + yPos + height, p.getZ()).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
                            buffer.pos(p.getX() + 1, p.getY() + yPos, p.getZ()).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
                        }
                        if (p.getZ() == size.getZ() - 1) {
                            buffer.pos(p.getX(), p.getY() + yPos, p.getZ() + 1).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
                            buffer.pos(p.getX() + 1, p.getY() + yPos, p.getZ() + 1).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
                            buffer.pos(p.getX() + 1, p.getY() + yPos + height, p.getZ() + 1).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
                            buffer.pos(p.getX(), p.getY() + yPos + height, p.getZ() + 1).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
                        }
                    }
                });
            }
        }
    }

}

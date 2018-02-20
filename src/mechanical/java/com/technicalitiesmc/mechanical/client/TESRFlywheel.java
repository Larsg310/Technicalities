package com.technicalitiesmc.mechanical.client;

import com.technicalitiesmc.mechanical.TKMechanical;
import com.technicalitiesmc.mechanical.tile.TileFlywheel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import static net.minecraft.client.renderer.GlStateManager.*;

public class TESRFlywheel extends TileEntitySpecialRenderer<TileFlywheel> {
    public static final ModelResourceLocation SHAFT_MODEL = new ModelResourceLocation(new ResourceLocation(TKMechanical.MODID, "flywheel"), "tesr");
    public static final ModelResourceLocation DISK_MODEL = new ModelResourceLocation(new ResourceLocation(TKMechanical.MODID, "stone_disk"), "inventory");

    @Override
    public void render(TileFlywheel te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getModel(SHAFT_MODEL);
        IBakedModel diskModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getModel(DISK_MODEL);
        BlockModelRenderer renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
        pushMatrix();
        translate(x + 0.5, y + 0.5, z + 0.5);
        rotate(te.getAngle(partialTicks), 0, 1, 0);
        translate(-0.5, -1f, -0.5);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        BlockPos pos = te.getPos();

        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderHelper.disableStandardItemLighting();
        blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        enableBlend();
        disableCull();

        if (Minecraft.isAmbientOcclusionEnabled()) {
            shadeModel(GL11.GL_SMOOTH);
        } else {
            shadeModel(GL11.GL_FLAT);
        }

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        buffer.setTranslation(-pos.getX(), -pos.getY(), -pos.getZ());
        renderer.renderModel(te.getWorld(), model, Blocks.STONE.getDefaultState(), te.getPos(), buffer, false, 0L);

        for (int i = 0; i < te.getDiskCount(); i++) {
            buffer.setTranslation(-te.getPos().getX(), -te.getPos().getY() + 2/16f * i, -te.getPos().getZ());
            renderer.renderModel(te.getWorld(), diskModel, Blocks.STONE.getDefaultState(), te.getPos(), buffer, false, 0L);
        }
        buffer.setTranslation(0, 0, 0);
        tessellator.draw();

        RenderHelper.enableStandardItemLighting();

        popMatrix();
    }
}

package com.technicalitiesmc.mechanical.client;

import com.technicalitiesmc.mechanical.tile.TileRotating;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import static java.lang.Math.abs;
import static net.minecraft.client.renderer.GlStateManager.*;

public class TESRRotating extends TileEntitySpecialRenderer<TileRotating> {

    @Override
    public void render(TileRotating te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        pushMatrix();
        translate(x + 0.5, y + 0.5, z + 0.5);
        Vec3i axis = te.getRotationFacing().getDirectionVec();
        rotate(te.getAngle(partialTicks), abs(axis.getX()), abs(axis.getY()), abs(axis.getZ()));
        float scale = te.getScale();
        scale(scale, scale, scale);
        translate(
                -0.5 - ((scale - 1) / 2F) * axis.getX(),
                -0.5 - ((scale - 1) / 2F) * axis.getY(),
                -0.5 - ((scale - 1) / 2F) * axis.getZ()
        );

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        World world = te.getWorld();
        BlockPos pos = te.getPos();
        IBlockState state = world.getBlockState(pos);
        IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);

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
        Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(world, model, state, pos, buffer, true);
        buffer.setTranslation(0, 0, 0);
        tessellator.draw();

        RenderHelper.enableStandardItemLighting();

        popMatrix();
    }

}

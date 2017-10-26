package com.technicalitiesmc.pneumatics.client;

import com.technicalitiesmc.pneumatics.TKPneumatics;
import com.technicalitiesmc.pneumatics.tile.TilePneumaticTubeClient;
import com.technicalitiesmc.pneumatics.tube.TubeStack;
import com.technicalitiesmc.util.Tint;
import com.technicalitiesmc.util.client.RenderHelper;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import static net.minecraft.client.renderer.GlStateManager.*;

public class TESRPneumaticTube extends TileEntitySpecialRenderer<TilePneumaticTubeClient> {

    @Override
    public void render(TilePneumaticTubeClient te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        pushMatrix();

        translate(x + 0.5, y + 0.5, z + 0.5);
        scale(0.5, 0.5, 0.5);

        for (TubeStack stack : te.stacks) {
            pushMatrix();

            float progress = stack.getProgress(), intProgress = progress + partialTicks * TubeStack.SPEED;
            EnumFacing direction = null;
            if (progress == 0.5F && stack.getTo() == null) {
                intProgress = 0;
                direction = stack.getFrom();
            } else if (progress < 0.5F) {
                intProgress = (0.5F - intProgress) * 2.0F;
                direction = stack.getFrom();
            } else if (progress > 0.5F) {
                intProgress = (intProgress - 0.5F) * 2.0F;
                direction = stack.getTo();
            }

            if (direction != null) {
                translate(intProgress * direction.getFrontOffsetX(), intProgress * direction.getFrontOffsetY(),
                        intProgress * direction.getFrontOffsetZ());
                scale(0.5, 0.5, 0.5);

                java.awt.Color color = Tint.getColor(stack.getColor());
                if (color != null) {
                    pushMatrix();
                    enableRescaleNormal();
                    enableBlend();
                    blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

                    bindTexture(new ResourceLocation(TKPneumatics.MODID, "textures/blocks/tube_stack_border.png"));

                    // Render corners outside
                    color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
                    RenderHelper.renderCuboid(Block.FULL_BLOCK_AABB.offset(-0.5, -0.5, -0.5));

                    scale(-1, -1, -1);

                    // Render corners inside
                    color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 0.5F);
                    RenderHelper.renderCuboid(Block.FULL_BLOCK_AABB.offset(-0.5, -0.5, -0.5));

                    // Render inner fill
                    disableTexture2D();
                    color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 0.25F);
                    RenderHelper.renderCuboid(Block.FULL_BLOCK_AABB.offset(-0.5, -0.5, -0.5));
                    enableTexture2D();

                    disableBlend();
                    popMatrix();
                }

                pushMatrix();
                scale(0.75, 0.75, 0.75);
                rotate((float) ((System.currentTimeMillis() / 30D) % 360D), 0, 1, 0);
                RenderHelper.renderStack(stack.getStack(), TransformType.NONE, alpha);
                popMatrix();
            }

            popMatrix();
        }

        popMatrix();
    }

}

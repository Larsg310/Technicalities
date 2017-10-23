package com.technicalitiesmc.mechanical.client;

import static net.minecraft.client.renderer.GlStateManager.*;

import org.apache.commons.lang3.tuple.Pair;

import com.technicalitiesmc.api.mechanical.conveyor.IConveyorBelt;
import com.technicalitiesmc.api.mechanical.conveyor.IConveyorObject;
import com.technicalitiesmc.base.init.TKBaseBlocks;
import com.technicalitiesmc.mechanical.conveyor.ConveyorBeltLogic;
import com.technicalitiesmc.util.client.RenderHelper;

import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class TESRConveyor extends TileEntitySpecialRenderer<TileEntity> {

    @Override
    public void render(TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        ConveyorBeltLogic conveyor = (ConveyorBeltLogic) te.getCapability(IConveyorBelt.CAPABILITY, null);

        pushMatrix();
        translate(x, y + conveyor.getHeight(), z);

        translate(0.5, 0.25, 0.5 - ((0.0625 * ((double) te.getWorld().getTotalWorldTime() + partialTicks)) % 1));
        if (te.getPos().getX() % 2 == 0) {
            RenderHelper.renderStack(new ItemStack(TKBaseBlocks.crate), TransformType.FIXED, alpha);
        } else {
            RenderHelper.renderStack(new ItemStack(TKBaseBlocks.barrel), TransformType.FIXED, alpha);
        }

        for (Pair<IConveyorObject, ConveyorBeltLogic.Path> object : conveyor.getObjects()) {
            if (object.getKey() instanceof IConveyorObject.Stack) {
                ItemStack stack = ((IConveyorObject.Stack) object.getKey()).getStack();
                ConveyorBeltLogic.Path path = object.getValue();

                RenderHelper.renderStack(stack, TransformType.FIXED, alpha);
            }
        }

        popMatrix();
    }

}

package com.technicalitiesmc.mechanical.client;

import com.technicalitiesmc.api.mechanical.conveyor.IConveyorBelt;
import com.technicalitiesmc.api.mechanical.conveyor.IConveyorObject;
import com.technicalitiesmc.lib.client.RenderHelper;
import com.technicalitiesmc.mechanical.conveyor.ConveyorBeltLogic;
import com.technicalitiesmc.mechanical.conveyor.IConveyorBeltHost;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.tuple.Pair;

import static net.minecraft.client.renderer.GlStateManager.*;

public class TESRConveyor extends TileEntitySpecialRenderer<TileEntity> {
    @Override
    public void render(TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        IConveyorBeltHost host = (IConveyorBeltHost) te;
        ConveyorBeltLogic conveyor = (ConveyorBeltLogic) te.getCapability(IConveyorBelt.CAPABILITY, null);
        if (conveyor == null) return;

        pushMatrix();
        translate(x + 0.5f, y + conveyor.getHeight() + 0.25f, z + 0.5f);

        // debug bb render to test convertToWorldCoords

        // for (Pair<IConveyorObject, ConveyorBeltLogic.Path> object : conveyor.getObjects().values()) {
        //     pushMatrix();
        //     translate(-0.5f, 0, -0.5f);
        //     Vec3d t = conveyor.convertToWorldCoords(object.getRight().getXOffset(), object.getRight().getProgress());
        //     translate(t.x, t.y, t.z);
        //     disableTexture2D();
        //     enableAlpha();
        //     enableBlend();
        //     color(1, 1, 1, 0.25f);
        //     object.getLeft().bounds().forEach(RenderHelper::renderCuboid);
        //     color(1, 1, 1, 1);
        //     enableTexture2D();
        //     popMatrix();
        // }

        if (host.getMovementAxis() == EnumFacing.Axis.X) {
            rotate(90, 0, 1, 0);
        }

        for (Pair<IConveyorObject, ConveyorBeltLogic.Path> object : conveyor.getObjects().values()) {
            pushMatrix();
            if (object.getKey() instanceof IConveyorObject.Stack) {
                ItemStack stack = ((IConveyorObject.Stack) object.getKey()).getStack();
                ConveyorBeltLogic.Path path = object.getValue();
                RenderHelper.glMultMatrix(path.transform(partialTicks));

                RenderHelper.renderStack(stack, TransformType.FIXED, alpha);
            }
            popMatrix();
        }

        popMatrix();
    }
}

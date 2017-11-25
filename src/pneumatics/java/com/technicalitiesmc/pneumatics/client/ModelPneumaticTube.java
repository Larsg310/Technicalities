package com.technicalitiesmc.pneumatics.client;

import com.technicalitiesmc.pneumatics.block.BlockPneumaticTube;
import com.technicalitiesmc.pneumatics.block.BlockPneumaticTube.Connection;
import com.technicalitiesmc.util.client.WrappedModel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.IExtendedBlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.technicalitiesmc.pneumatics.client.TKPClientProxy.*;

public class ModelPneumaticTube extends WrappedModel {

    public ModelPneumaticTube(IBakedModel parent) {
        super(parent);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side != null || (state != null && !(state instanceof IExtendedBlockState))) {
            return Collections.emptyList();
        }

        // Item rendering
        if (state == null) {
            List<BakedQuad> quads = new ArrayList<>();
            for (EnumFacing face : EnumFacing.VALUES) {
                IBakedModel model = MODELS.get(face.getAxis() == Axis.Y ? TUBE_OPEN : TUBE_CLOSED, face);
                if(model == null){
                    model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel();
                }
                quads.addAll(model.getQuads(state, side, rand));
            }
            return quads;
        }
        IExtendedBlockState extendedState = (IExtendedBlockState) state;

        // Check if it's a straight tube
        EnumFacing.Axis axis = null;
        boolean straight = false;
        for (EnumFacing face : EnumFacing.VALUES) {
            BlockPneumaticTube.Connection con = (Connection) extendedState.getValue(BlockPneumaticTube.CONNECTIONS[face.ordinal()]);
            if (con.connected || con.module != null) {
                if (axis == null) {
                    if (con.connected && (con.module == null || !con.module.renderTube())) {
                        axis = face.getAxis();
                    }
                } else if (axis == face.getAxis()) {
                    straight = con.connected && (con.module == null || !con.module.renderTube());
                } else {
                    straight = false;
                    break;
                }
            }
        }

        // Generate quads
        List<BakedQuad> quads = new ArrayList<>();

        if (straight) {
            quads.addAll(MODELS.get(TUBE_STRAIGHT, EnumFacing.getFacingFromAxis(AxisDirection.NEGATIVE, axis)).getQuads(state, side, rand));
        }

        for (EnumFacing face : EnumFacing.VALUES) {
            BlockPneumaticTube.Connection con = (Connection) extendedState.getValue(BlockPneumaticTube.CONNECTIONS[face.ordinal()]);

            if (con.module != null) {
                ResourceLocation path = con.module.getModel();
                if (path != null) {
                    for (BakedQuad quad : MODELS.get(path, face).getQuads(state, side, rand)) {
                        if (quad.hasTintIndex()) {
                            quads.add(new BakedQuad(quad.getVertexData(), con.module.getTint(quad.getTintIndex()), quad.getFace(),
                                    quad.getSprite(), quad.shouldApplyDiffuseLighting(), quad.getFormat()));
                        } else {
                            quads.add(quad);
                        }
                    }
                }

                if (!con.module.renderTube()) {
                    if (!straight) {
                        quads.addAll(MODELS.get(TUBE_CAP, face).getQuads(state, side, rand));
                    }
                    continue;
                }
            }

            if (!straight && (con.module == null || con.module.renderTube())) {
                if (con.connected) {
                    quads.addAll(MODELS.get(TUBE_OPEN, face).getQuads(state, side, rand));
                } else {
                    quads.addAll(MODELS.get(TUBE_CLOSED, face).getQuads(state, side, rand));
                }
            }
        }
        return quads;
    }

}

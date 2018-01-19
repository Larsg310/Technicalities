package com.technicalitiesmc.electricity.client.model;

import com.technicalitiesmc.lib.client.WrappedModel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModelElectricWire extends WrappedModel {

    public ModelElectricWire(IBakedModel parent) {
        super(parent);
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side != null) {
            return Collections.emptyList();
        }

        List<BakedQuad> quads = new ArrayList<>();

        return quads;
    }

}

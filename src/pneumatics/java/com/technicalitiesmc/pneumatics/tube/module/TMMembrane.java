package com.technicalitiesmc.pneumatics.tube.module;

import com.technicalitiesmc.api.pneumatics.IPneumaticTube;
import com.technicalitiesmc.api.pneumatics.ITubeStack;
import com.technicalitiesmc.api.pneumatics.TubeModule;
import com.technicalitiesmc.pneumatics.TKPneumatics;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Consumer;

public class TMMembrane extends TubeModule {

    private static final ResourceLocation PATH_SINGLE = new ResourceLocation(TKPneumatics.MODID, "block/tube/membrane/single");

    private TMMembrane(IPneumaticTube tube, EnumFacing side) {
        super(tube, side);
    }

    @Override
    public boolean requiresConnection() {
        return true;
    }

    @Override
    public boolean preventsConnection() {
        return false;
    }

    @Override
    public boolean renderTube() {
        return true;
    }

    @Override
    public ResourceLocation getModel() {
        return PATH_SINGLE;
    }

    @Override
    public int getTraversalPriority(ITubeStack stack) {
        return -50;
    }

    public static class Type extends TubeModule.Type<TMMembrane> {

        @Override
        public TMMembrane placeSingle(IPneumaticTube tube, EnumFacing side) {
            return new TMMembrane(tube, side);
        }

        @Override
        public Pair<TMMembrane, TMMembrane> placePair(IPneumaticTube tube, EnumFacing side, IPneumaticTube other) {
            return Pair.of(new TMMembrane(tube, side), null);
        }

        @Override
        public TMMembrane instantiate(IPneumaticTube tube, EnumFacing side) {
            return new TMMembrane(tube, side);
        }

        @Override
        public void link(TMMembrane filter1, TMMembrane filter2) {
        }

        @Override
        public void registerModels(Consumer<ResourceLocation> registry) {
            registry.accept(PATH_SINGLE);
        }

    }

}

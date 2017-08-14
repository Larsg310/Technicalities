package com.technicalitiesmc.pneumatics.tube.module;

import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.api.pneumatics.IPneumaticTube;
import com.technicalitiesmc.api.pneumatics.TubeModule;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class TMPlug extends TubeModule {

    private static final ResourceLocation PATH_SINGLE = new ResourceLocation(Technicalities.MODID, "block/tube/plug/single");

    private TMPlug(IPneumaticTube tube, EnumFacing side) {
        super(tube, side);
    }

    @Override
    public boolean requiresConnection() {
        return false;
    }

    @Override
    public boolean preventsConnection() {
        return true;
    }

    @Override
    public boolean renderTube() {
        return true;
    }

    @Override
    public ResourceLocation getModel() {
        return PATH_SINGLE;
    }

    public static class Type extends TubeModule.Type<TMPlug> {

        @Override
        public TMPlug placeSingle(IPneumaticTube tube, EnumFacing side) {
            return new TMPlug(tube, side);
        }

        @Override
        public Pair<TMPlug, TMPlug> placePair(IPneumaticTube tube, EnumFacing side, IPneumaticTube other) {
            return Pair.of(new TMPlug(tube, side), null);
        }

        @Override
        public TMPlug instantiate(IPneumaticTube tube, EnumFacing side) {
            return new TMPlug(tube, side);
        }

        @Override
        public void link(TMPlug filter1, TMPlug filter2) {
        }

        @Override
        public void registerModels(Consumer<ResourceLocation> registry) {
            registry.accept(PATH_SINGLE);
        }

    }

}

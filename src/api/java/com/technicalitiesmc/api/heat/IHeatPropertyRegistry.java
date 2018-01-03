package com.technicalitiesmc.api.heat;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Created by Elec332 on 30-12-2017.
 */
public interface IHeatPropertyRegistry {

    default public void registerHeatMaterial(Block block, IThermalMaterial material){
        registerHeatMaterial(block.getBlockState().getValidStates(), material);
    }

    default public void registerHeatMaterial(Iterable<IBlockState> states, IThermalMaterial material){
        states.forEach(state -> registerHeatMaterial(state, material));
    }

    public void registerHeatMaterial(IBlockState state, IThermalMaterial material);

    @Nonnull
    public IThermalMaterial getMaterial(IBlockState state);

    @Nonnull
    public IThermalMaterial getMaterial(ResourceLocation name);

}

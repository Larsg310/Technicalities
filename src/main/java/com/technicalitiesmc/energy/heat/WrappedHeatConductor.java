package com.technicalitiesmc.energy.heat;

import com.google.common.base.Preconditions;
import com.technicalitiesmc.api.TechnicalitiesAPI;
import com.technicalitiesmc.api.heat.IHeatConductor;
import com.technicalitiesmc.api.heat.IThermalMaterial;
import elec332.core.util.RegistryHelper;
import elec332.core.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;

/**
 * Created by Elec332 on 2-1-2018.
 */
public final class WrappedHeatConductor implements IHeatConductor, IThermalMaterial, INBTSerializable<NBTTagCompound> {

    private WrappedHeatConductor(){
    }

    public WrappedHeatConductor(IBlockState state, IHeatConductor conductor){
        this.state = state;
        this.conductor = conductor;
        this.material = Preconditions.checkNotNull(conductor.getMaterial());
        Preconditions.checkState(TechnicalitiesAPI.heatPropertyRegistry.getMaterial(material.getRegistryName()) == material, "Invalid material: "+material);
        this.c = true;
    }

    public WrappedHeatConductor(IBlockState state){
        this.state = state;
        this.conductor = null;
        this.material = HeatPropertyRegistry.INSTANCE.getMaterial(state);
        this.c = false;
    }

    private IBlockState state;
    private IHeatConductor conductor;
    private IThermalMaterial material;
    private boolean c;

    protected boolean check(World world, BlockPos pos){
        if (state != WorldHelper.getBlockState(world, pos)){
            return false;
        }
        if (!c || conductor != null){
            return true;
        }
        TileEntity tile = WorldHelper.getTileAt(world, pos);
        if (!tile.hasCapability(TechnicalitiesAPI.HEAT_CONDUCTOR_CAP, null)){
            return false;
        }
        conductor = Preconditions.checkNotNull(tile.getCapability(TechnicalitiesAPI.HEAT_CONDUCTOR_CAP, null));
        return true;
    }

    protected IBlockState getState(){
        return state;
    }

    protected boolean hasTile(){
        return c;
    }

    @Override
    public IThermalMaterial getMaterial() {
        return conductor == null ? material : conductor.getMaterial();
    }

    @Override
    public boolean conductsHeat(IBlockState state) {
        return conductor == null ? material.conductsHeat(state) : conductor.conductsHeat(state);
    }

    @Override
    public double getM3(IBlockState state) {
        return conductor == null ? material.getM3(state) : conductor.getM3(state);
    }

    public double getMass(IBlockState state) {
        return getM3(state) * getDensity();
    }

    @Override
    public boolean touches(@Nonnull EnumFacing side) {
        return conductor == null || conductor.touches(side);
    }

    public static WrappedHeatConductor read(NBTTagCompound tag){
        WrappedHeatConductor ret = new WrappedHeatConductor();
        ret.deserializeNBT(tag);
        return ret;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound ret = new NBTTagCompound();
        System.out.println("serialize: "+state.getBlock().getRegistryName());
        ret.setString("Bn", Preconditions.checkNotNull(state.getBlock().getRegistryName()).toString());
        ret.setInteger("Bm", state.getBlock().getMetaFromState(state));
        ret.setBoolean("c", c);
        ret.setString("TMn", Preconditions.checkNotNull(material.getRegistryName()).toString());
        return ret;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void deserializeNBT(NBTTagCompound nbt) {
        Block b = RegistryHelper.getBlockRegistry().getValue(new ResourceLocation(nbt.getString("Bn")));
        if (b == Blocks.AIR || b == null){
            throw new RuntimeException();
        }
        this.state = b.getStateFromMeta(nbt.getInteger("Bm"));
        this.c = nbt.getBoolean("c");
        this.material = HeatPropertyRegistry.INSTANCE.getMaterial(new ResourceLocation(nbt.getString("TMn")));
    }

    //Link-through

    @Override
    public double getSpecificHeatCapacity() {
        return getMaterial().getSpecificHeatCapacity();
    }

    @Override
    public double getThermalConductivity() {
        return getMaterial().getThermalConductivity();
    }

    @Override
    public double getDensity() {
        return getMaterial().getDensity();
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryName() {
        return getMaterial().getRegistryName();
    }

}

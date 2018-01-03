package com.technicalitiesmc.energy.heat;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.technicalitiesmc.api.heat.IHeatObject;
import com.technicalitiesmc.api.heat.IThermalMaterial;
import elec332.core.grid.IPositionable;
import elec332.core.world.DimensionCoordinate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created by Elec332 on 27-12-2017.
 */
public class PositionedHeatData implements IPositionable, IHeatObject, INBTSerializable<NBTTagCompound> {

    public PositionedHeatData(World world, BlockPos pos, WrappedHeatConductor heatThing){
        this.coord = new DimensionCoordinate(world, pos);
        this.heatStuff = Preconditions.checkNotNull(heatThing);
        this.setCheckedTemperature(HeatConstants.getAmbientTemperature(world, pos));
    }

    private DimensionCoordinate coord;
    private WrappedHeatConductor heatStuff;
    private double temp, energy;
    private int worked;

    private double ambientTemp;

    @Nonnull
    @Override
    public DimensionCoordinate getPosition() {
        return coord;
    }

    @Override
    public double getEnergy() {
        return energy;
    }

    @Override
    public double getTemperature() {
        return temp;
    }

    @Override
    public boolean hasChanged() {
        return false;
    }

    protected boolean isConductor(){
        return heatStuff.conductsHeat(heatStuff.getState());
    }

    protected boolean isSignificant(){
        return heatStuff.hasTile();
    }

    protected double getMass(){
        return heatStuff.getMass(heatStuff.getState());
    }

    protected double getMaxEnergyTransfer(double temp){
        return Math.pow((heatStuff.getThermalConductivity() * 100), 2) * temp;
    }

    protected IThermalMaterial getThermalMaterial(){
        return heatStuff;
    }

    protected boolean shouldRemovePreTick(World world){
        ambientTemp = HeatConstants.getAmbientTemperature(world, coord.getPos());
        if (worked > 0){
            worked--;
        }
        boolean c = !heatStuff.check(world, coord.getPos());
        //System.out.println("checkHS: "+c);
        return c || !stay();
    }

    private boolean stay(){
        return (!isConductor() && worked > 0) || (Math.abs(ambientTemp - temp) > 1);
    }

    void setCheckedTemperature(double temp){
        if (!isConductor()){
            worked = 20;
            return;
        }
        if (temp < 0){
            temp = 0;
        }
        this.temp = temp;
        this.energy = getMass() * heatStuff.getSpecificHeatCapacity() * temp;
    }

    void modifyEnergy(double modifier){
        if (!isConductor()){
            worked = 20;
            return;
        }
        //System.out.println("ME: "+modifier);
        energy += modifier;
        if (this.energy < 0){
            this.energy = 0;
        }
        double temp = this.temp;
        this.temp = energy / (getMass() * heatStuff.getSpecificHeatCapacity());
        //System.out.println(this.temp+"   "+temp);
    }

    public void update(World world, WorldHeatHandler whh) {
        if (!this.isConductor()) {
            this.temp = this.ambientTemp;
        } else {
            BlockPos pos = coord.getPos();
            List<PositionedHeatData> neighborsH = Lists.newArrayList();
            List<PositionedHeatData> neighborsC = Lists.newArrayList();
            for (EnumFacing facing : EnumFacing.VALUES){
                PositionedHeatData hd = whh.getOrCreate(world, pos.offset(facing));
                if (hd.temp < temp){
                    neighborsC.add(hd);
                } else if (!hd.isConductor()){
                    neighborsH.add(hd);
                }
            }
            neighborsH.forEach(this::transfer); //Passive, hotter objects should transfer their heat to this object first
            neighborsC.forEach(this::transfer);
        }
    }

    private void transfer(PositionedHeatData other){
        transfer(this, other);
    }

    private static void transfer(PositionedHeatData one, PositionedHeatData two){
        double tempD = one.temp - two.temp;
        double maxE = Math.min(one.getMaxEnergyTransfer(tempD), two.getMaxEnergyTransfer(tempD));
        one.modifyEnergy(-maxE);
        two.modifyEnergy(maxE);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound ret = new NBTTagCompound();
        ret.setTag("coord", coord.serializeNBT());
        ret.setTag("heatObj", heatStuff.serializeNBT());
        ret.setDouble("temp", temp);
        ret.setDouble("energy", energy);
        //ret.setInteger("work", worked);
        return ret;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        coord = DimensionCoordinate.fromNBT(nbt.getCompoundTag("coord"));
        heatStuff = WrappedHeatConductor.read(nbt.getCompoundTag("heatObj"));
        temp = nbt.getDouble("temp");
        energy = nbt.getDouble("energy");
        //worked = nbt.getInteger("work");
    }

    private PositionedHeatData(){
    }

    public static PositionedHeatData fromNBT(NBTTagCompound tag){
        PositionedHeatData ret = new PositionedHeatData();
        ret.deserializeNBT(tag);
        return ret;
    }

}

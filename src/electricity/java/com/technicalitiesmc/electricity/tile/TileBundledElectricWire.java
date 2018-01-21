package com.technicalitiesmc.electricity.tile;

import com.technicalitiesmc.electricity.util.ColorHelper;
import com.technicalitiesmc.lib.block.TileBase;
import elec332.core.java.JavaHelper;
import elec332.core.world.WorldHelper;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by Elec332 on 19-1-2018.
 */
public class TileBundledElectricWire extends TileBase implements ITickable {

    private int colors = 0;
    public EnumSet<EnumFacing> connections = EnumSet.noneOf(EnumFacing.class), change = EnumSet.noneOf(EnumFacing.class);

    private static final EnumSet<EnumFacing> NS, EW;

    public boolean isStraightLine(){
        return isStraightLine(connections);
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("wrclr", colors);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.colors = compound.getInteger("wrclr");
    }

    public boolean hasWire(EnumDyeColor color) {
        return ColorHelper.hasWire(color, colors);
    }

    public void addWire(EnumDyeColor color) {
        colors = ColorHelper.addWire(color, colors);
    }

    public void removeWire(EnumDyeColor color) {
        colors = ColorHelper.removeWire(color, colors);
    }

    public void setColors(List<EnumDyeColor> colors){
        this.colors = 0;
        for (EnumDyeColor color : colors){
            addWire(color);
        }
    }

    public int getColorBits() {
        return colors;
    }

    @Override
    public void update() {
        connections.clear();
        change.clear();
        for (EnumFacing facing : EnumFacing.VALUES){
            TileEntity tile = WorldHelper.getTileAt(world, pos.offset(facing));
            if (tile instanceof TileBundledElectricWire){
                TileBundledElectricWire wire = (TileBundledElectricWire) tile;
                if (JavaHelper.hasAtLeastOneMatch(ColorHelper.getColors(wire.getColorBits()), ColorHelper.getColors(getColorBits()))) {
                    connections.add(facing);
                    if (((TileBundledElectricWire) tile).colors != colors) {
                        change.add(facing);
                    }
                }
            }
        }
    }

    public static boolean isStraightLine(EnumSet<EnumFacing> connections){
        return connections.equals(NS) || connections.equals(EW);
    }

    static {
        NS = EnumSet.of(EnumFacing.SOUTH, EnumFacing.NORTH);
        EW = EnumSet.of(EnumFacing.EAST, EnumFacing.WEST);
    }

}

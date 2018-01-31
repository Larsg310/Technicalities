package com.technicalitiesmc.electricity.tile;

import com.google.common.collect.Sets;
import com.technicalitiesmc.electricity.item.ItemBundledWire;
import com.technicalitiesmc.electricity.util.ColorHelper;
import com.technicalitiesmc.electricity.util.EnumBitSet;
import com.technicalitiesmc.electricity.util.WireColor;
import elec332.core.world.WorldHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by Elec332 on 25-1-2018.
 */
public class WirePart {

    public WirePart(EnumFacing placement){
        this.placement = placement;
    }

    public EnumFacing placement;

    private int colors = 0;
    public EnumBitSet<EnumFacing> connections = EnumBitSet.noneOf(EnumFacing.class), change = EnumBitSet.noneOf(EnumFacing.class);
    public BitSet ud = new BitSet(EnumFacing.VALUES.length);

    @Nonnull
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("wrclr", colors);
        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        this.colors = compound.getInteger("wrclr");
    }

    public boolean canStay(World world, BlockPos pos){
        return WorldHelper.getBlockState(world, pos.offset(placement)).isSideSolid(world, pos, placement.getOpposite());
    }

    public ItemStack getDropStack(){
        return ItemBundledWire.withCables(ColorHelper.getColors(getColorBits()));
    }

    public void addBoxes(IBlockState state, World world, BlockPos pos, List<AxisAlignedBB> boxes) {
        if (placement != EnumFacing.DOWN){
            return;
        }
        float width = ColorHelper.getColors(getColorBits()).size();
        float stuff = ((16 - width) / 2) / 16;
        float stuff2 = .5f;
        if (connections.size() != 1) {
            float ft = stuff;
            if (!isStraightLine()) {
                ft = (16 - (width + 2)) / 32f;
            }
            boxes.add(new AxisAlignedBB(ft, 0, ft, 1 - ft, 1 / 16f, 1 - ft));
            stuff2 = stuff;
        }
        for (EnumFacing facing : connections){
            boolean z = facing.getAxis() == EnumFacing.Axis.Z;
            boolean n = facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;
            AxisAlignedBB aabb = new AxisAlignedBB(z ? stuff : 1, 0, z ? 1 - stuff2 : stuff, 1 - (z ? stuff : stuff2), 1/16f, z ? 1 : 1 - stuff);
            if (n){
                float offset = -(1 - stuff2);
                aabb = aabb.offset(z ? 0 : offset, 0, z ? offset : 0);
            }
            boxes.add(aabb);
        }
    }

    public int getColorBits() {
        return colors;
    }

    public boolean isStraightLine(){
        return TileBundledElectricWire.isStraightLine(connections);
    }

    public boolean hasWire(WireColor color) {
        return ColorHelper.hasWire(color, colors);
    }

    public boolean addWires(Collection<WireColor> colorz){
        Set<WireColor> colors = Sets.newHashSet(colorz);
        if (colors.size() != colorz.size()){
            return false;
        }
        for (WireColor color : colors){
            if (hasWire(color)){
                return false;
            }
        }
        colors.forEach(this::addWire);
        //syncColors(true);
        return true;
    }

    public boolean addWire(WireColor color) {
        if (hasWire(color)){
            return false;
        }
        colors = ColorHelper.addWire(color, colors);
        //notifyNeighborsOfChangeExtensively();
        //WorldHelper.markBlockForUpdate(world, pos);
        //syncColors(true);
        return true;
    }

    public boolean removeWire(WireColor color) {
        if (hasWire(color)) {
            colors = ColorHelper.removeWire(color, colors);
            //notifyNeighborsOfChangeExtensively();
            //WorldHelper.markBlockForUpdate(world, pos);
           //syncColors(true);
            return true;
        }
        return false;
    }

    public void setColors(List<WireColor> colors){
        this.colors = 0;
        for (WireColor color : colors){
            addWire(color);
        }
        //notifyNeighborsOfChangeExtensively();
        //WorldHelper.markBlockForUpdate(world, pos);
        //syncColors(true);
    }

}

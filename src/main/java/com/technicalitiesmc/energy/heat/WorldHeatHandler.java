package com.technicalitiesmc.energy.heat;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.technicalitiesmc.api.TechnicalitiesAPI;
import com.technicalitiesmc.api.heat.IWorldHeatHandler;
import elec332.core.util.NBTTypes;
import elec332.core.world.DimensionCoordinate;
import elec332.core.world.PositionedObjectHolder;
import elec332.core.world.WorldHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * Created by Elec332 on 28-12-2017.
 */
public final class WorldHeatHandler implements IWorldHeatHandler, INBTSerializable<NBTTagCompound> {

    public WorldHeatHandler(){
        dataMap = new PositionedObjectHolder<>();
        heatToProcess = Queues.newArrayDeque();
    }

    private final PositionedObjectHolder<PositionedHeatData> dataMap;
    private final Queue<CachedHeatInfo> heatToProcess;

    protected PositionedHeatData getOrCreate(World world, BlockPos pos){
        PositionedHeatData phd = dataMap.get(pos);
        if (phd == null){
            phd = createNew(world, pos);
            dataMap.put(phd, pos);
        }
        return phd;
    }

    @Nullable
    @Override
    public PositionedHeatData getHeatObject(BlockPos pos) {
        return dataMap.get(pos);
    }

    private PositionedHeatData createNew(World world, BlockPos pos){
        IBlockState state = WorldHelper.getBlockState(world, pos);
        WrappedHeatConductor bla = null;
        if (state.getBlock().hasTileEntity(state)){
            TileEntity tile = WorldHelper.getTileAt(world, pos);
            if (tile != null && tile.hasCapability(TechnicalitiesAPI.HEAT_CONDUCTOR_CAP, null)) {
                bla = new WrappedHeatConductor(state, Preconditions.checkNotNull(tile.getCapability(TechnicalitiesAPI.HEAT_CONDUCTOR_CAP, null)));
            }
        }
        if (bla == null){
            bla = new WrappedHeatConductor(state);
        }
        return new PositionedHeatData(world, pos, bla);
    }

    @Override
    public void addEnergyToBlock(TileEntity from, BlockPos to, double energy, double temp) {
        energy = energy * HeatConstants.getPowerScalar();
        if (Math.abs(energy) < 10){ //Not worth the calculation costs
            return;
        }
        heatToProcess.offer(new CachedHeatInfo(new DimensionCoordinate(from.getWorld(), to), energy, temp));
    }

    @Override
    public void addEnergyToSurroundings(TileEntity tile, double energy, double temp, EnumFacing... sides) {
        energy = energy * HeatConstants.getPowerScalar();
        if (Math.abs(energy) < 10){ //Not worth the calculation costs
            return;
        }
        heatToProcess.offer(new CachedHeatInfo(DimensionCoordinate.fromTileEntity(tile), energy, temp, sides));
    }

    protected PositionedObjectHolder<PositionedHeatData> getWorldData(){
        return dataMap;
    }

    public void update(World world, Collection<Chunk> chunks){
        if (WorldHelper.getDimID(world) != 0){
            return;
        }
        CachedHeatInfo chi = heatToProcess.poll();
        while (chi != null){
            BlockPos pos = chi.dimCoord.getPos();
            if (!WorldHelper.chunkLoaded(world, pos)){
                chi = heatToProcess.poll();
                continue;
            }
            if (chi.dirs == null){
                PositionedHeatData obj = getOrCreate(world, pos);
                if (obj.isConductor()){
                    double objTemp = obj.getTemperature();
                    double tempD = Math.abs(objTemp - chi.temp);
                    double energy = Math.min(Math.abs(chi.energy), tempD * obj.getMaxEnergyTransfer(tempD));
                    if (chi.neg()){
                        energy = -energy;
                        if (objTemp < chi.temp){
                            chi = heatToProcess.poll();
                            continue;
                        }
                    } else if (chi.temp < objTemp){
                        chi = heatToProcess.poll();
                        continue;
                    }
                    obj.modifyEnergy(energy);
                    /*double tD = energy / (material.getSpecificHeatCapacity() * obj.getMass());
                    objTemp += tD;*/
                    /* Subtracting energy, tObj must be > tGiven | Adding energy, tObj must be < tGiven*/
                    /*if ((chi.neg() && objTemp < chi.temp) || (objTemp > chi.temp)){
                        objTemp = chi.temp;
                    }
                    obj.setCheckedTemperature(objTemp);*/
                }
            } else {
                List<PositionedHeatData> dhdL = Lists.newArrayList(chi.dirs)
                        .stream().map(f -> {
                            PositionedHeatData phd = getOrCreate(world, pos.offset(f));
                            if (!phd.isConnected(f.getOpposite())){
                                return null;
                            }
                            return phd;
                        })
                        .filter(Objects::nonNull)
                        //.sorted((o1, o2) -> (int) -(o1.getTemperature() - o2.getTemperature()))
                        .collect(Collectors.toList());
                /*
                double energyToDis = chi.energy;
                for (int i = 0; i < dhdL.size(); i++) {
                    for (int j = 0; j < i; j++) {
                        PositionedHeatData w = dhdL.get(j);

                    }
                }*/
                double maxR = 0;
                double[] em = new double[dhdL.size()];
                for (int i = 0; i < dhdL.size(); i++) {
                    PositionedHeatData phd = dhdL.get(i);
                    double mod = phd.getMaxEnergyTransfer(Math.abs(phd.getTemperature() - chi.temp));
                    if (chi.neg()){
                        mod = -mod;
                        if (chi.temp > phd.getTemperature()){
                            mod = 0;
                        }
                    } else if (chi.temp < phd.getTemperature()){
                        mod = 0;
                    }
                    maxR += mod;
                    em[i] = mod;
                }
                double factor = Math.min(1, Math.abs(chi.energy / maxR));
                for (int i = 0; i < dhdL.size(); i++) {
                    if (em[i] > 0) {
                        dhdL.get(i).modifyEnergy(em[i] * factor);
                    }
                }

            }
            chi = heatToProcess.poll();
        }
        List<BlockPos> remove = Lists.newArrayList();
        chunks.forEach(chunk -> dataMap.getObjectsInChunk(chunk.getPos()).forEach((pos, h) -> {
            boolean b = h.shouldRemovePreTick(world);
            if (b){
                remove.add(pos);
            }
        }));
        //System.out.println(remove);
        remove.forEach(dataMap::remove);
        chunks.forEach(chunk -> Lists.newArrayList(dataMap.getObjectsInChunk(chunk.getPos()).values()).forEach(
                h -> h.update(world, this)
        ));
        //System.out.println("---");
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound ret = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        dataMap.getChunks().forEach(chunkPos -> dataMap.getObjectsInChunk(chunkPos).forEach((pos, positionedHeatData) -> {
            if (positionedHeatData.isConductor()) {
                list.appendTag(positionedHeatData.serializeNBT());
            }
        }));
        ret.setTag("hpd", list);
        return ret;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        NBTTagList list = nbt.getTagList("hpd", NBTTypes.COMPOUND.getID());
        for (int i = 0; i < list.tagCount(); i++) {
            PositionedHeatData tA = PositionedHeatData.fromNBT(list.getCompoundTagAt(i));
            //
            dataMap.put(tA, tA.getPosition().getPos());
        }
    }

    private class CachedHeatInfo {

        private CachedHeatInfo(DimensionCoordinate dc, double energy, double temp, EnumFacing... dirs){
            this.energy = energy;
            this.temp = temp;
            this.dimCoord = dc;
            this.dirs = (dirs == null || dirs.length < 1) ? null : dirs;
        }

        private double energy, temp;
        private DimensionCoordinate dimCoord;
        private EnumFacing[] dirs;

        private boolean neg(){
            return energy < 0;
        }

    }

}

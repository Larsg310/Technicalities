package com.technicalitiesmc.api.mechanical;

import com.technicalitiesmc.api.TechnicalitiesAPI;
import com.technicalitiesmc.api.util.ObjFloatConsumer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.function.BiPredicate;

public interface IKineticNode extends INBTSerializable<NBTTagCompound> {

    static IKineticNode create(IKineticNode.Host host) {
        return TechnicalitiesAPI.kineticNodeProvider.apply(host);
    }

    void validate(boolean isRemote);

    void invalidate();

    float getTorque();

    float getVelocity();

    float getPower();

    float getAngle();

    default float getAngle(float partialTick) {
        return getAngle() + partialTick * getVelocity();
    }

    float getInertia(); // Inherited from host

    float getAppliedPower(); // Inherited from host

    float getConsumedPower(); // Inherited from host

    interface Host {

        World getKineticWorld();

        ChunkPos getKineticChunk();

        float getInertia();

        default float getAppliedPower() {
            return 0;
        }

        default float getConsumedPower() {
            return getInertia();
        }

        void addNeighbors(ObjFloatConsumer<IKineticNode> neighbors, BiPredicate<World, BlockPos> posValidator);

    }

    static void findShaft(World world, BlockPos pos, EnumFacing side,float ratio,
                          ObjFloatConsumer<IKineticNode> neighbors, BiPredicate<World, BlockPos> posValidator){
        pos = pos.offset(side);
        if (posValidator.test(world, pos)) {
            TileEntity te = world.getTileEntity(pos);
            if (te != null && te.hasCapability(IShaftAttachable.CAPABILITY, side.getOpposite())) {
                neighbors.accept(te.getCapability(IShaftAttachable.CAPABILITY, side.getOpposite()).getNode(), ratio);
            }
        }
    }

    static void findGear(World world, BlockPos pos, EnumFacing face, EnumFacing side, float ratio,
                         ObjFloatConsumer<IKineticNode> neighbors, BiPredicate<World, BlockPos> posValidator){
        pos = pos.offset(side);
        if (posValidator.test(world, pos)) {
            TileEntity te = world.getTileEntity(pos);
            if (te != null && te.hasCapability(IGearAttachable.CAPABILITY, side.getOpposite())) {
                neighbors.accept(te.getCapability(IGearAttachable.CAPABILITY, side.getOpposite()).getNode(face), ratio);
            }
        }
    }

}

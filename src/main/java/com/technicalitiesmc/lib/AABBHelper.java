package com.technicalitiesmc.lib;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * Created by Elec332 on 3-2-2018.
 */
public class AABBHelper {

    @SuppressWarnings("all")
    public static AxisAlignedBB rotateFromDown(AxisAlignedBB aabb, EnumFacing facing){
        switch (facing){
            case UP:
                return new AxisAlignedBB(aabb.minX, 1 - aabb.minY, 1 - aabb.minZ, aabb.maxX, 1 - aabb.maxY, 1 - aabb.maxZ);
            case NORTH:
                return new AxisAlignedBB(aabb.minX, 1 - aabb.minZ, aabb.minY, aabb.maxX, 1 - aabb.maxZ, aabb.maxY);
            case EAST:
                return new AxisAlignedBB(1 - aabb.minY, aabb.minX, aabb.minZ, 1 - aabb.maxY, aabb.maxX, aabb.maxZ);
            case SOUTH:
                return new AxisAlignedBB(aabb.minX, aabb.minZ, 1 - aabb.minY, aabb.maxX, aabb.maxZ, 1 - aabb.maxY);
            case WEST:
                return new AxisAlignedBB(aabb.minY, aabb.minX, aabb.minZ, aabb.maxY, aabb.maxX, aabb.maxZ);
            default:
                return aabb;
        }
    }

}

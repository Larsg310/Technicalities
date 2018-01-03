package com.technicalitiesmc.base.item;

import com.technicalitiesmc.api.TechnicalitiesAPI;
import com.technicalitiesmc.api.heat.IHeatObject;
import com.technicalitiesmc.lib.item.ItemBase;
import elec332.core.util.PlayerHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * Created by Elec332 on 3-1-2018.
 */
public class ItemHeatProbe extends ItemBase {

    @Nonnull
    @Override
    public EnumActionResult onItemUseC(EntityPlayer player, EnumHand hand, World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isRemote){
            IHeatObject heatObject = TechnicalitiesAPI.getHeatHandler(world).getHeatObject(pos);
            PlayerHelper.sendMessageToPlayer(player, "Pos: "+pos);
            if (heatObject == null){
                PlayerHelper.sendMessageToPlayer(player, "null");
                return EnumActionResult.SUCCESS;
            }
            PlayerHelper.sendMessageToPlayer(player, "Temp: "+heatObject.getTemperature());
            PlayerHelper.sendMessageToPlayer(player, "Energy: "+heatObject.getEnergy());
        }
        return EnumActionResult.SUCCESS;
    }

}

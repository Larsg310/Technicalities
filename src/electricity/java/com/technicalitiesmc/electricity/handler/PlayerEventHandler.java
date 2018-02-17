package com.technicalitiesmc.electricity.handler;

import com.technicalitiesmc.electricity.init.ItemRegister;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by Elec332 on 23-11-2017.
 */
public class PlayerEventHandler {

    @SubscribeEvent
    public void onBlockActivated(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().getItem() == ItemRegister.wireCoil) {
            event.setUseBlock(Event.Result.DENY);
        }
    }

}

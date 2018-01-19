package com.technicalitiesmc.electricity.proxies;

import com.technicalitiesmc.electricity.client.ClientHandler;
import com.technicalitiesmc.electricity.client.model.ModelElectricWire;
import com.technicalitiesmc.electricity.init.BlockRegister;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by Elec332 on 23-11-2017.
 */
public class TKEClientProxy extends TKECommonProxy {

	@Override
	public void initRendering() {
		MinecraftForge.EVENT_BUS.register(new ClientHandler());
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
    public void onModelBake(ModelBakeEvent event){
        ModelResourceLocation path = new ModelResourceLocation(BlockRegister.electric_wire.getRegistryName(), "normal");
	    IBakedModel wireModel = event.getModelRegistry().getObject(path);
	    event.getModelRegistry().putObject(path, new ModelElectricWire(wireModel));
    }

}

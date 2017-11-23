package com.technicalitiesmc.electricity.proxies;

import com.technicalitiesmc.electricity.client.ClientHandler;
import net.minecraftforge.common.MinecraftForge;

/**
 * Created by Elec332 on 23-11-2017.
 */
public class TKEClientProxy extends TKECommonProxy {

	@Override
	public void initRendering() {
		MinecraftForge.EVENT_BUS.register(new ClientHandler());
	}

}

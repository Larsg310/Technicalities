package com.technicalitiesmc.electricity.proxies;

import com.technicalitiesmc.electricity.client.ClientHandler;
import com.technicalitiesmc.electricity.client.ModelCache;
import com.technicalitiesmc.electricity.client.model.ModelCacheElectricWire;
import com.technicalitiesmc.electricity.client.model.ModelCacheTest;
import com.technicalitiesmc.electricity.init.BlockRegister;
import elec332.core.client.model.RenderingRegistry;
import elec332.core.client.model.loading.IModelAndTextureLoader;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by Elec332 on 23-11-2017.
 */
public class TKEClientProxy extends TKECommonProxy {

    private static ModelCache modelCacheElectricWire, modelCacheTest;

    @Override
    public void initRendering() {
        MinecraftForge.EVENT_BUS.register(new ClientHandler());
        MinecraftForge.EVENT_BUS.register(this);
        modelCacheElectricWire = new ModelCacheElectricWire();
        modelCacheTest = new ModelCacheTest();
        RenderingRegistry.instance().registerLoader((IModelAndTextureLoader) modelCacheElectricWire);
        /*ModelLoader.setCustomStateMapper(BlockRegister.modelTest, new StateMapperBase() {
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
				return null;
			}
		});*/
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        ModelResourceLocation path = new ModelResourceLocation(BlockRegister.electric_bundled_wire.getRegistryName(), "normal");
        event.getModelRegistry().putObject(path, modelCacheElectricWire);
        path = new ModelResourceLocation(BlockRegister.modelTest.getRegistryName(), "normal");
        event.getModelRegistry().putObject(path, modelCacheTest);
    }

}

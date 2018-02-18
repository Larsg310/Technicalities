package com.technicalitiesmc.electricity.proxies;

import com.google.common.collect.Lists;
import com.technicalitiesmc.api.electricity.EnumElectricityType;
import com.technicalitiesmc.electricity.client.ClientHandler;
import com.technicalitiesmc.electricity.client.ModelCache;
import com.technicalitiesmc.electricity.client.model.ModelCacheElectricWire;
import com.technicalitiesmc.electricity.client.model.ModelCacheTest;
import com.technicalitiesmc.electricity.init.BlockRegister;
import com.technicalitiesmc.electricity.item.ItemBundledWire;
import com.technicalitiesmc.electricity.wires.WireColor;
import elec332.core.client.model.RenderingRegistry;
import elec332.core.client.model.loading.IModelAndTextureLoader;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.EnumDyeColor;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Created by Elec332 on 23-11-2017.
 */
public class TKEClientProxy extends TKECommonProxy {

    public static ModelCache modelCacheElectricWire, modelCacheTest;

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
        overrideModel(event, BlockRegister.electric_bundled_wire, "normal", modelCacheElectricWire);
        //overrideModel(event, BlockRegister.electric_bundled_wire, "inventory", modelCacheElectricWire.getModel(ItemBundledWire.withCables(Lists.newArrayList(WireColor.getWireColor(EnumDyeColor.WHITE, EnumElectricityType.AC), WireColor.getWireColor(EnumDyeColor.YELLOW, EnumElectricityType.AC), WireColor.getWireColor(EnumDyeColor.CYAN, EnumElectricityType.AC)), 2)));
        overrideModel(event, BlockRegister.modelTest, "normal", modelCacheTest);
    }

    @SuppressWarnings("all")
    private void overrideModel(ModelBakeEvent event, IForgeRegistryEntry<?> obj, String variant, IBakedModel model){
        event.getModelRegistry().putObject(new ModelResourceLocation(obj.getRegistryName(), variant), model);
    }

}

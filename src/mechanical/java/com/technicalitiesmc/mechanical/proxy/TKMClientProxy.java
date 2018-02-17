package com.technicalitiesmc.mechanical.proxy;

import com.technicalitiesmc.mechanical.TKMechanical;
import com.technicalitiesmc.mechanical.client.TESRFlywheel;
import com.technicalitiesmc.mechanical.init.TKMechanicalBlocks;

import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class TKMClientProxy extends TKMCommonProxy {

    public static final Map<ResourceLocation, IBakedModel> MODELS = new HashMap<>();

    public static final ResourceLocation HAND_CRANK_LEVER = new ResourceLocation(TKMechanical.MODID, "block/hand_crank_lever");

    private static final TRSRTransformation BASE_TRANSFORM = new TRSRTransformation(EnumFacing.NORTH);

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        OBJLoader.INSTANCE.addDomain(TKMechanical.MODID);
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(TKMechanicalBlocks.flywheel), 1, TESRFlywheel.SHAFT_MODEL);
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent event) {
        if (event.getMap() != Minecraft.getMinecraft().getTextureMapBlocks()) return;

        loadTextures(HAND_CRANK_LEVER, event.getMap());
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        MODELS.clear();

        loadAndBake(HAND_CRANK_LEVER);
    }

    private void loadTextures(ResourceLocation path, TextureMap textureMap) {
        try {
            ModelLoaderRegistry.getModel(path).getTextures().forEach(textureMap::registerSprite);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadAndBake(ResourceLocation path) {
        try {
            IModel rawModel = ModelLoaderRegistry.getModel(path);

            IBakedModel model = rawModel.bake(BASE_TRANSFORM, DefaultVertexFormats.BLOCK,
                Minecraft.getMinecraft().getTextureMapBlocks()::registerSprite);
            MODELS.put(path, model);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

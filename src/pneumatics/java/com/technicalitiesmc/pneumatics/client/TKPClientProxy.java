package com.technicalitiesmc.pneumatics.client;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.technicalitiesmc.pneumatics.TKPCommonProxy;
import com.technicalitiesmc.pneumatics.TKPneumatics;
import com.technicalitiesmc.pneumatics.init.TKPneumaticsBlocks;
import com.technicalitiesmc.pneumatics.tube.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;

public class TKPClientProxy extends TKPCommonProxy {

    public static final Table<ResourceLocation, EnumFacing, IBakedModel> MODELS = HashBasedTable.create();

    public static final ResourceLocation TUBE_OPEN = new ResourceLocation(TKPneumatics.MODID, "block/tube/open");
    public static final ResourceLocation TUBE_CLOSED = new ResourceLocation(TKPneumatics.MODID, "block/tube/closed");
    public static final ResourceLocation TUBE_CAP = new ResourceLocation(TKPneumatics.MODID, "block/tube/cap");
    public static final ResourceLocation TUBE_STRAIGHT = new ResourceLocation(TKPneumatics.MODID, "block/tube/straight");

    private static final TRSRTransformation BASE_TRANSFORM = new TRSRTransformation(EnumFacing.DOWN).inverse();

    @Override
    public void init() {
        super.init();
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((s, w, p, i) -> i, TKPneumaticsBlocks.pneumatic_tube);
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent event) {
        if (event.getMap() != Minecraft.getMinecraft().getTextureMapBlocks()) {
            return;
        }

        // Load tube model textures
        loadTextures(TUBE_OPEN, event.getMap());
        loadTextures(TUBE_CLOSED, event.getMap());
        loadTextures(TUBE_CAP, event.getMap());
        loadTextures(TUBE_STRAIGHT, event.getMap());

        // Load tube module textures
        ModuleManager.INSTANCE.getModuleTypes().forEach(type -> type.registerModels(res -> loadTextures(res, event.getMap())));
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        // Block model
        ModelResourceLocation tubePath = new ModelResourceLocation(TKPneumaticsBlocks.pneumatic_tube.getRegistryName(), "normal");
        IBakedModel tubeModel = event.getModelRegistry().getObject(tubePath);
        event.getModelRegistry().putObject(tubePath, new ModelPneumaticTube(tubeModel));

        // Item model
        tubePath = new ModelResourceLocation(TKPneumaticsBlocks.pneumatic_tube.getRegistryName(), "inventory");
        tubeModel = event.getModelRegistry().getObject(tubePath);
        event.getModelRegistry().putObject(tubePath, new ModelPneumaticTube(tubeModel));

        // Clear previous set of models
        MODELS.clear();

        // Load tube model parts
        loadAndBake(TUBE_OPEN);
        loadAndBake(TUBE_CLOSED);
        loadAndBake(TUBE_CAP);
        loadAndBake(TUBE_STRAIGHT);

        // Load tube module models
        ModuleManager.INSTANCE.getModuleTypes().forEach(type -> type.registerModels(this::loadAndBake));
    }

    private void loadTextures(ResourceLocation path, TextureMap textureMap) {
        try {
            ModelLoaderRegistry.getModel(path).getTextures().forEach(textureMap::registerSprite);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadAndBake(ResourceLocation path) {
        Map<EnumFacing, IBakedModel> map = MODELS.row(path);
        try {
            IModel rawModel = ModelLoaderRegistry.getModel(path);

            for (EnumFacing face : EnumFacing.values()) {
                IBakedModel model = rawModel.bake(new TRSRTransformation(face).compose(BASE_TRANSFORM), DefaultVertexFormats.BLOCK,
                        Minecraft.getMinecraft().getTextureMapBlocks()::registerSprite);
                map.put(face, model);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}

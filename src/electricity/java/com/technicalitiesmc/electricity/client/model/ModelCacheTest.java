package com.technicalitiesmc.electricity.client.model;

import com.google.common.collect.ImmutableMap;
import com.technicalitiesmc.electricity.TKElectricity;
import com.technicalitiesmc.electricity.client.ModelCache;
import com.technicalitiesmc.electricity.util.TKEResourceLocation;
import elec332.core.api.client.model.ModelLoadEvent;
import elec332.core.client.model.RenderingRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

/**
 * Created by Elec332 on 12-2-2018.
 */
@SuppressWarnings("all") //test class
public class ModelCacheTest extends ModelCache<Integer> {

    private ModelResourceLocation[] mrl;
    private IBakedModel[] models;

    public ModelCacheTest() {
        debug = true;
        int count = 4;

        mrl = new ModelResourceLocation[count];
        for (int i = 0; i < count; i++) {
            mrl[i] = new ModelResourceLocation(TKElectricity.MODID + ":wireterm"+(i + 1), "normal");
            RenderingRegistry.instance().registerLoadableModel(mrl[i]);
        }
        models = new IBakedModel[count];

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void afterAllModelsBaked(ModelLoadEvent event) throws Exception {
        for (int i = 0; i < mrl.length; i++) {
            //models[i] = event.getModel(mrl[i]);
            IModel model = ModelLoaderRegistry.getModel(new ResourceLocation(mrl[i].getResourceDomain(), mrl[i].getResourcePath()));
            model = model.retexture(ImmutableMap.<String, String>builder().put("missing", "tkelectricity:blocks/normal").build());
            System.out.println(model.getTextures());
            models[i] = model.bake(model.getDefaultState(), DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter());
        }
        IModel model = ModelLoaderRegistry.getModel(new ResourceLocation("tkelectricity:coal_generator"));
        System.out.println(model.getTextures());
    }

    @Nonnull
    @Override
    protected ResourceLocation getTextureLocation() {
        return new TKEResourceLocation("test");
    }

    @Override
    protected Integer get(IBlockState state) {
        return new Random().nextInt(4);
    }

    @Override
    protected Integer get(ItemStack stack) {
        return 1;
    }

    @Override
    protected void bakeQuads(List<BakedQuad> quads, EnumFacing side, Integer key) {
        quads.addAll(models[key].getQuads(null, side, 0));
    }

}

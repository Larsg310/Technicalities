package com.technicalitiesmc.electricity.client.model;

import com.technicalitiesmc.electricity.TKElectricity;
import com.technicalitiesmc.electricity.client.ModelCache;
import com.technicalitiesmc.electricity.init.BlockRegister;
import com.technicalitiesmc.electricity.util.TKEResourceLocation;
import elec332.core.client.RenderHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;

/**
 * Created by Elec332 on 12-2-2018.
 */
public class ModelCacheTest extends ModelCache<Integer> {

    private ModelResourceLocation[] mrl;
    private IBakedModel model;

    public ModelCacheTest() {
        debug = true;
        int count = 4;

        mrl = new ModelResourceLocation[count];
        for (int i = 0; i < count; i++) {
            mrl[i] = new ModelResourceLocation(TKElectricity.MODID + ":receivertest", "normal");
        }
        //models = new IBakedModel[count];
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void bakeQuads(List<BakedQuad> quads, EnumFacing side, Integer key) {
        IBakedModel model = this.model;
        System.out.println("bakeQuads");
        if (model == null) {
            System.out.println("null");
            model = RenderHelper.getMissingModel();
        }
        model.getQuads(BlockRegister.modelTest.getDefaultState(), side, 100);
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void afterAllModelsBaked(ModelBakeEvent event) {
        //for (int i = 0; i < mrl.length; i++) {
        IBakedModel model;
        try {
            IModel model_ = ModelLoaderRegistry.getModel(new ResourceLocation(TKElectricity.MODID, "receivertest"));
            //model_.retexture(ImmutableMap.copyOf(bla));
            model = model_.bake(model_.getDefaultState(), DefaultVertexFormats.BLOCK,
                    new Function<ResourceLocation, TextureAtlasSprite>() {
                        @Override
                        public TextureAtlasSprite apply(ResourceLocation resourceLocation) {
                            return ModelLoader.White.INSTANCE;
                        }
                    });
        } catch (Exception var7) {
            model = RenderHelper.getMissingModel();
            FMLLog.log.error("Exception loading blockstate for the variant {}: ", "BLAARG", var7);
        }
        this.model = model;
        //}
        System.out.println("loaded test models");

        //for (int i = 0; i < mrl.length; i++) {
        //    RenderingRegistry.instance().registerLoadableModel(mrl[i]);
        //}
        //for (int i = 0; i < mrl.length; i++) {
        //    models[i] = event.getModel(mrl[i]);
        //}
        //System.out.println("loaded test models");
        //FMLCommonHandler.instance().exitJava(1, true);
    }

    @Nonnull
    @Override
    protected ResourceLocation getTextureLocation() {
        return new TKEResourceLocation("test");
    }

    @Override
    protected Integer get(IBlockState state) {
        return 1;
    }

    @Override
    protected Integer get(ItemStack stack) {
        return 1;
    }
}

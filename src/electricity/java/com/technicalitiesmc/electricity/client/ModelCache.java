package com.technicalitiesmc.electricity.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.technicalitiesmc.lib.client.WrappedModel;
import elec332.core.client.model.ElecModelBakery;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Elec332 on 20-1-2018.
 */
@SideOnly(Side.CLIENT)
public abstract class ModelCache<K> implements IBakedModel {

    public ModelCache(){
        quads = CacheBuilder.newBuilder().expireAfterAccess(2, TimeUnit.MINUTES).build();
        itemModels = CacheBuilder.newBuilder().expireAfterAccess(2, TimeUnit.MINUTES).build();
        iol = new ItemOverrideList(ImmutableList.of()){

            @Override
            @Nonnull
            public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
                return getModel(stack);
            }

        };
    }

    private final Cache<K, Map<EnumFacing, List<BakedQuad>>> quads;
    private final Cache<K, IBakedModel> itemModels;
    private final ItemOverrideList iol;

    protected abstract K get(IBlockState state);

    protected abstract K get(ItemStack stack);

    protected abstract void bakeQuads(List<BakedQuad> quads, EnumFacing side, K key);

    protected final Map<EnumFacing, List<BakedQuad>> getQuads(K key){
        try {
            return quads.get(key, () -> {
                Map<EnumFacing, List<BakedQuad>> ret = Maps.newHashMap();
                for (EnumFacing f : EnumFacing.VALUES){
                    List<BakedQuad> q = Lists.newArrayList();
                    bakeQuads(q, f, key);
                    ret.put(f, ImmutableList.copyOf(q));
                }
                List<BakedQuad> q = Lists.newArrayList();
                bakeQuads(q, null, key);
                ret.put(null, ImmutableList.copyOf(q));
                return ret;
            });
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public final IBakedModel getModel(ItemStack stack){
        K key = get(stack);
        try {
            return itemModels.get(key, () -> new WrappedModel(ModelCache.this) {

                Map<EnumFacing, List<BakedQuad>> quads = ModelCache.this.getQuads(key);

                @Override
                public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
                    return quads.get(side);
                }

            });
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    @Nonnull
    public final List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        return getQuads(get(state)).get(side);
    }

    @Override
    public boolean isAmbientOcclusion(){
        return true;
    }

    @Override
    public boolean isGui3d(){
        return true;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    @Nonnull
    public TextureAtlasSprite getParticleTexture() {
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(getTextureLocation().toString());
    }

    @Nonnull
    protected abstract ResourceLocation getTextureLocation();

    @Override
    @Nonnull
    @SuppressWarnings("deprecation")
    public ItemCameraTransforms getItemCameraTransforms() {
        return ElecModelBakery.DEFAULT_BLOCK;
    }

    @Override
    @Nonnull
    public final ItemOverrideList getOverrides() {
        return iol;
    }

}

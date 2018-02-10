package com.technicalitiesmc.electricity.client.model;

import com.technicalitiesmc.api.electricity.EnumElectricityType;
import com.technicalitiesmc.electricity.block.BlockBundledElectricWire;
import com.technicalitiesmc.electricity.client.ModelCache;
import com.technicalitiesmc.electricity.tile.WirePart;
import com.technicalitiesmc.electricity.util.*;
import elec332.core.api.client.IIconRegistrar;
import elec332.core.api.client.model.IElecModelBakery;
import elec332.core.api.client.model.IElecQuadBakery;
import elec332.core.api.client.model.IElecTemplateBakery;
import elec332.core.client.RenderHelper;
import elec332.core.client.model.loading.IModelAndTextureLoader;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.model.ITransformation;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;
import java.util.List;

/**
 * Created by Elec332 on 20-1-2018.
 */
public class ModelCacheElectricWire extends ModelCache<BlockBundledElectricWire.RenderData> implements IModelAndTextureLoader {

    public ModelCacheElectricWire(){
        debug = true;
    }

    private IElecQuadBakery quadBakery;
    private TextureAtlasSprite connector, black;
    private TextureAtlasSprite[] individualWires, wireTypes;

    @Override
    protected void bakeQuads(List<BakedQuad> quads, EnumFacing side, BlockBundledElectricWire.RenderData data_) {
        if (side != null) {
            return;
        }
        for (WirePart data : data_.getWires()) {
            int size = data.getWireSize();
            EnumFacing ef = data.getPlacement();
            int x = ef.getAxis() == EnumFacing.Axis.Z ? 180 - (90 * ef.getAxisDirection().getOffset()) : ef == EnumFacing.UP ? 180 : 0;
            int z = ef.getAxis() == EnumFacing.Axis.X ? 180 - (90 * ef.getAxisDirection().getOffset()) : 0;
            ITransformation placementTransformation = RenderHelper.getTransformation(x, 0, z);
            List<WireColor> colors = ColorHelper.getColors(data.getColorBits());
            if (data_.isItem() && Config.singleWirePNGRendering && colors.size() == 1) {
                quads.addAll(quadBakery.getGeneralItemQuads(individualWires[colors.get(0).getColor().ordinal()]));
                return;
            }
            float posStart;
            int total = colors.size();
            EnumBitSet<EnumFacing> conn = data.connections;
            float ft = (16 - (total * size + 2)) / 2f;
            for (EnumFacing facing : conn) {
                boolean neg = facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;
                boolean change = data.change.contains(facing);
                boolean extend = data.extended.get(facing.ordinal()) && !change;
                boolean exnp = extend && data.isExtended(facing);
                boolean shortened = data.shortened.get(facing.ordinal());
                boolean shrt = shortened && data.isExtended(facing);
                int zero8 = neg ? (shrt ? size : 0) : (exnp ? 8 - size : 8);
                int eight16 = neg ? (exnp ? 8 + size : 8) : (shrt ? 16 - size : 16);
                int extStart = exnp ? -size : shrt ? size : 0;
                posStart = ft + 1;
                ITransformation baseTransformation = RenderHelper.defaultFor(facing);
                ITransformation placedBaseTransformation = merge(baseTransformation, placementTransformation);
                for (int i = 0; i < colors.size(); i++) {
                    boolean extraNeg = ef == EnumFacing.UP && facing.getAxis() == EnumFacing.Axis.X;
                    extraNeg |= ef == EnumFacing.EAST && facing.getAxis() == EnumFacing.Axis.Z;
                    extraNeg |= ef == EnumFacing.NORTH && facing.getAxis() == EnumFacing.Axis.X;
                    WireColor wireColor = colors.get((extraNeg != neg) ? i : colors.size() - 1 - i);
                    EnumDyeColor color = wireColor.getColor();
                    TextureAtlasSprite wire = wireTypes[wireColor.getType().ordinal()];
                    if (i == 0) {
                        ITransformation i0T = merge(RenderHelper.getTransformation(0, 0, 90), baseTransformation);
                        quads.add(quadBakery.bakeQuad(new Vector3f(size, 16 - posStart, 8), new Vector3f(0, 16 - posStart, extStart), wire, EnumFacing.UP, merge(i0T, placementTransformation), color.ordinal() + 1, eight16, color.ordinal(), zero8));
                    }
                    if (conn.size() == 1) {
                        quads.add(quadBakery.bakeQuad(new Vector3f(posStart, 0, 8), new Vector3f(posStart + size, size, 8), wire, EnumFacing.SOUTH, placedBaseTransformation, color.ordinal(), 0.0F, color.ordinal() + 1, 2.0F));
                    }
                    quads.add(quadBakery.bakeQuad(new Vector3f(posStart, size, extend ? -size : (shortened ? size : 0)), new Vector3f(posStart + size, size, 8), wire, EnumFacing.UP, placedBaseTransformation, color.ordinal(), neg ? (shortened ? size : 0) : (extend ? 8 - size : 8), color.ordinal() + 1, neg ? (extend ? 8 + size : 8) : (shortened ? 16 - size : 16)));
                    posStart += size;
                    if (i == colors.size() - 1) {
                        ITransformation iCt = merge(RenderHelper.getTransformation(0, 180, 90), baseTransformation);
                        quads.add(quadBakery.bakeQuad(new Vector3f(size, posStart, 16 - extStart), new Vector3f(0, posStart, 8), wire, EnumFacing.UP, merge(iCt, placementTransformation), color.ordinal() + 1, eight16, color.ordinal(), zero8));
                    }
                }
                if (change) {
                    extend = data.extended.get(facing.ordinal());
                    int min11 = extend ? -1 : 1;
                    quads.add(quadBakery.bakeQuad(new Vector3f(ft, 1.1f * size, 0), new Vector3f(16 - ft, 1.1f * size, 1.1f * min11), black, extend ? EnumFacing.DOWN : EnumFacing.UP, placedBaseTransformation));
                    if (extend){
                        quads.add(quadBakery.bakeQuad(new Vector3f(ft, 0, 0), new Vector3f(16 - ft, 0, -1.1f), black, EnumFacing.UP, placedBaseTransformation));
                    }
                    for (EnumFacing f : EnumFacing.VALUES) {
                        if (f.getAxis() != EnumFacing.Axis.Y) {
                            quads.add(quadBakery.bakeQuad(new Vector3f(f == EnumFacing.EAST ? 16 - ft : ft, 0, f == EnumFacing.SOUTH ?  (1.1f * min11) : 0), new Vector3f(f == EnumFacing.WEST ? ft : 16 - ft, 1.1f * size, f == EnumFacing.NORTH ? 0 : (1.1f * min11)), black, extend ? f.getOpposite() : f, placedBaseTransformation));
                        }
                    }
                }
            }

            if (conn.size() != 1 && !data.isStraightLine()) {
                quads.add(quadBakery.bakeQuad(new Vector3f(ft, 1.1f * size, ft), new Vector3f(16 - ft, 1.1f * size, 16 - ft), black, EnumFacing.UP, placementTransformation));
                for (EnumFacing facing : EnumFacing.VALUES) {
                    if (facing.getAxis() != EnumFacing.Axis.Y) {
                        quads.add(quadBakery.bakeQuad(new Vector3f(ft, 0, ft), new Vector3f(16 - ft, 1.1f * size, ft), black, EnumFacing.NORTH, merge(RenderHelper.defaultFor(facing), placementTransformation)));
                    }
                }
            }
        }
    }

    private static ITransformation merge(ITransformation t1, ITransformation t2){
        Matrix4f m = new Matrix4f(t2.getMatrix());
        m.mul(t1.getMatrix());
        return new TRSRTransformation(m);
    }

    @Override
    public void registerTextures(IIconRegistrar iiconRegistrar) {
        wireTypes = new TextureAtlasSprite[EnumElectricityType.values().length];
        for (int i = 0; i < wireTypes.length; i++) {
            wireTypes[i] = iiconRegistrar.registerSprite(new TKEResourceLocation("blocks/flatwire_"+EnumElectricityType.values()[i].toString().toLowerCase()));
        }
        connector = iiconRegistrar.registerSprite(new TKEResourceLocation("blocks/flatwire_connector"));
        black = iiconRegistrar.registerSprite(new TKEResourceLocation("blocks/black"));
        individualWires = new TextureAtlasSprite[EnumDyeColor.values().length];
        for (int i = 0; i < individualWires.length; i++) {
            individualWires[i] = iiconRegistrar.registerSprite(new TKEResourceLocation("items/wire_"+EnumDyeColor.values()[i].getDyeColorName()));
        }
    }

    @Override
    public void registerModels(IElecQuadBakery quadBakery, IElecModelBakery modelBakery, IElecTemplateBakery templateBakery) {
        this.quadBakery = quadBakery;
    }

    @Override
    protected BlockBundledElectricWire.RenderData get(IBlockState state) {
        return ((IExtendedBlockState) state).getValue(BlockBundledElectricWire.PROPERTY_RENDERDATA);
    }

    @Override
    protected BlockBundledElectricWire.RenderData get(ItemStack stack) {
        return BlockBundledElectricWire.fromItem(stack);
    }

    @Nonnull
    @Override
    protected ResourceLocation getTextureLocation() {
        return new TKEResourceLocation("blocks/black");
    }

}

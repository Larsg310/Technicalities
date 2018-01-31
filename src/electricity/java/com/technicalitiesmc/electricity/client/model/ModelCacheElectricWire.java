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
import java.util.EnumSet;
import java.util.List;

/**
 * Created by Elec332 on 20-1-2018.
 */
public class ModelCacheElectricWire extends ModelCache<BlockBundledElectricWire.RenderData> implements IModelAndTextureLoader {

    private IElecQuadBakery quadBakery;
    private TextureAtlasSprite connector, black;
    private TextureAtlasSprite[] individualWires, wireTypes;

    @Override
    protected void bakeQuads(List<BakedQuad> quads, EnumFacing side, BlockBundledElectricWire.RenderData data_) {
        if (side != null) {
            return;
        }
        ITransformation placementTransformation = TRSRTransformation.identity();
        for (WirePart data : data_.getWires()) {
            List<WireColor> colors = ColorHelper.getColors(data.getColorBits());
            if (data_.isItem() && Config.singleWirePNGRendering && colors.size() == 1) {
                quads.addAll(quadBakery.getGeneralItemQuads(individualWires[colors.get(0).getColor().ordinal()]));
                return;
            }
            float posStart;
            int total = colors.size();
            EnumBitSet<EnumFacing> conn = data.connections;
            float ft = (16 - (total + 2)) / 2f;
            for (EnumFacing facing : conn) {
                boolean neg = facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;
                posStart = ft + 1;
                ITransformation baseTransformation = RenderHelper.defaultFor(facing);
                for (int i = 0; i < colors.size(); i++) {
                    WireColor wireColor = colors.get(neg ? i : colors.size() - 1 - i);
                    EnumDyeColor color = wireColor.getColor();
                    TextureAtlasSprite wire = wireTypes[wireColor.getType().ordinal()];
                    if (i == 0) {
                        quads.add(quadBakery.bakeQuad(new Vector3f(1, 16 - posStart, 8), new Vector3f(0, 16 - posStart, 0), wire, EnumFacing.UP, merge(RenderHelper.getTransformation(0, 0, 90), baseTransformation), color.ordinal() + 1, neg ? 8 : 16, color.ordinal(), neg ? 0 : 8));
                    }
                    if (conn.size() == 1) {
                        quads.add(quadBakery.bakeQuad(new Vector3f(posStart, 0, 8), new Vector3f(posStart + 1, 1, 8), wire, EnumFacing.SOUTH, baseTransformation, color.ordinal(), 0.0F, color.ordinal() + 1, 2.0F));
                    }
                    quads.add(quadBakery.bakeQuad(new Vector3f(posStart, 1, 0), new Vector3f(posStart + 1, 1, 8), wire, EnumFacing.UP, baseTransformation, color.ordinal(), (neg ? 0 : 8), color.ordinal() + 1, (neg ? 8 : 16)));
                    posStart += 1;
                    if (i == colors.size() - 1) {
                        quads.add(quadBakery.bakeQuad(new Vector3f(1, posStart, 16), new Vector3f(0, posStart, 8), wire, EnumFacing.UP, merge(RenderHelper.getTransformation(0, 180, 90), baseTransformation), color.ordinal() + 1, (neg ? 8 : 16), color.ordinal(), (neg ? 0 : 8)));
                    }
                }
                if (data.change.contains(facing)) {
                    quads.add(quadBakery.bakeQuad(new Vector3f(ft, 1.1f, 0), new Vector3f(16 - ft, 1.1f, 1), black, EnumFacing.UP, baseTransformation));
                    for (EnumFacing f : EnumFacing.VALUES) {
                        if (f.getAxis() != EnumFacing.Axis.Y) {
                            quads.add(quadBakery.bakeQuad(new Vector3f(f == EnumFacing.EAST ? 16 - ft : ft, 0, 0), new Vector3f(f == EnumFacing.WEST ? ft : 16 - ft, 1.1f, f == EnumFacing.NORTH ? 0 : 1), black, f, baseTransformation));
                        }
                    }
                }
            }

            if (conn.size() != 1 && !data.isStraightLine()) {
                quads.add(quadBakery.bakeQuad(new Vector3f(ft, 1.1f, ft), new Vector3f(16 - ft, 1.1f, 16 - ft), black, EnumFacing.UP));
                for (EnumFacing facing : EnumFacing.VALUES) {
                    if (facing.getAxis() != EnumFacing.Axis.Y) {
                        quads.add(quadBakery.bakeQuad(new Vector3f(ft, 0, ft), new Vector3f(16 - ft, 1.1f, ft), black, EnumFacing.NORTH, RenderHelper.defaultFor(facing)));
                    }
                }
            }
        }
    }

    private static ITransformation merge(ITransformation t1, ITransformation t2){
        Matrix4f m = ((Matrix4f) t2.getMatrix().clone());
        m.mul(((Matrix4f) t1.getMatrix().clone()));
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

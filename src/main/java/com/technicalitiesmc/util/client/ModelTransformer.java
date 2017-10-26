package com.technicalitiesmc.util.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumType;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

import java.util.LinkedList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Utility class that allows the transformation of {@link IBakedModel}s and {@link BakedQuad}s through the use of an
 * {@link IModelTransformer}.
 */
public class ModelTransformer {

    /**
     * Transforms the model in real time.
     */
    public static IBakedModel transformDynamic(IBakedModel model, IModelTransformer transformer) {
        return transformDynamic(model, transformer, f -> f);
    }

    /**
     * Transforms the model in real time and remaps its {@link BakedQuad}s' formats.
     */
    public static IBakedModel transformDynamic(IBakedModel model, IModelTransformer transformer, UnaryOperator<VertexFormat> remapper) {
        return new DynWrapper(model, transformer, remapper);
    }

    /**
     * Pre-bakes the transformed model and returns it as a static object.
     */
    public static IBakedModel transform(IBakedModel model, IBlockState state, long rand, IModelTransformer transformer) {
        return transform(model, state, rand, transformer, f -> f);
    }

    /**
     * Pre-bakes the transformed model and remaps its {@link BakedQuad}s' formats, then returns it as a static object.
     */
    @SuppressWarnings("unchecked")
    public static IBakedModel transform(IBakedModel model, IBlockState state, long rand, IModelTransformer transformer,
                                        UnaryOperator<VertexFormat> remapper) {
        List<BakedQuad>[] quads = new List[7];
        for (EnumFacing f : EnumFacing.VALUES) {
            quads[f.ordinal()] = new LinkedList<>(model.getQuads(state, f, rand));
            quads[f.ordinal()].replaceAll(q -> transform(q, f, transformer, remapper.apply(q.getFormat())));
        }
        quads[6] = new LinkedList<>(model.getQuads(state, null, rand));
        quads[6].replaceAll(q -> transform(q, null, transformer, remapper.apply(q.getFormat())));
        return createModel(model, quads);
    }

    private static IBakedModel createModel(IBakedModel model, List<BakedQuad>[] quads) {
        return new WrappedModel(model) {

            @Override
            public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
                return quads[side == null ? 6 : side.ordinal()];
            }
        };
    }

    /**
     * Transforms a {@link BakedQuad quad}.
     */
    public static BakedQuad transform(BakedQuad quad, EnumFacing face, IModelTransformer transformer) {
        return transform(quad, face, transformer, quad.getFormat());
    }

    /**
     * Transforms a {@link BakedQuad quad} and remaps its format.
     */
    public static BakedQuad transform(BakedQuad quad, EnumFacing face, IModelTransformer transformer, VertexFormat newFormat) {
        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(newFormat) {

            @Override
            public void put(int elementIndex, float... data) {
                VertexFormatElement element = newFormat.getElement(elementIndex);
                if (element.getUsage() == EnumUsage.COLOR) {
                    data[0] = 1;
                    data[1] = 1;
                    data[2] = 1;
                }
                transformer.transform(face, quad, element.getType(), element.getUsage(), data);
                super.put(elementIndex, data);
            }
        };
        quad.pipe(builder);
        return builder.build();
    }

    public interface IModelTransformer {

        public void transform(EnumFacing face, BakedQuad quad, EnumType type, EnumUsage usage, float... data);

    }

    private static final class DynWrapper extends WrappedModel {

        private final IModelTransformer transformer;
        private final UnaryOperator<VertexFormat> remapper;

        public DynWrapper(IBakedModel parent, IModelTransformer transformer, UnaryOperator<VertexFormat> remapper) {
            super(parent);
            this.transformer = transformer;
            this.remapper = remapper;
        }

        @Override
        public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
            List<BakedQuad> list = new LinkedList<>(parent.getQuads(state, side, rand));
            list.replaceAll(q -> transform(q, side, transformer, remapper.apply(q.getFormat())));
            return list;
        }

    }

}

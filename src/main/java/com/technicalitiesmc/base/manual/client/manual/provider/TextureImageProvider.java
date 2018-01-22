package com.technicalitiesmc.base.manual.client.manual.provider;

import com.technicalitiesmc.base.Technicalities;
import com.technicalitiesmc.base.manual.api.manual.ImageProvider;
import com.technicalitiesmc.base.manual.api.manual.ImageRenderer;
import com.technicalitiesmc.base.manual.client.manual.segment.render.MissingItemRenderer;
import com.technicalitiesmc.base.manual.client.manual.segment.render.TextureImageRenderer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public final class TextureImageProvider implements ImageProvider {
    private static final String WARNING_IMAGE_MISSING = Technicalities.MODID + ".manual.warning.missing.image";

    @Override
    @Nullable
    public ImageRenderer getImage(final String data) {
        try {
            return new TextureImageRenderer(new ResourceLocation(data));
        } catch (final Throwable t) {
            return new MissingItemRenderer(WARNING_IMAGE_MISSING);
        }
    }
}

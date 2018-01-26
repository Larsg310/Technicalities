package com.technicalitiesmc.base.manual.client.manual.provider;

import com.google.common.base.Strings;
import com.technicalitiesmc.base.Technicalities;
import com.technicalitiesmc.base.manual.api.manual.ImageProvider;
import com.technicalitiesmc.base.manual.api.manual.ImageRenderer;
import com.technicalitiesmc.base.manual.client.manual.segment.render.ItemStackImageRenderer;
import com.technicalitiesmc.base.manual.client.manual.segment.render.MissingItemRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public final class ItemImageProvider implements ImageProvider {
    private static final String WARNING_ITEM_MISSING = Technicalities.MODID + ".manual.warning.missing.item";

    @Override
    @Nullable
    public ImageRenderer getImage(final String data) {
        final int splitIndex = data.lastIndexOf('@');
        final String name, optMeta;
        if (splitIndex > 0) {
            name = data.substring(0, splitIndex);
            optMeta = data.substring(splitIndex);
        } else {
            name = data;
            optMeta = "";
        }
        final int meta = (Strings.isNullOrEmpty(optMeta)) ? 0 : Integer.parseInt(optMeta.substring(1));
        final Item item = Item.REGISTRY.getObject(new ResourceLocation(name));
        if (item != null) {
            return new ItemStackImageRenderer(new ItemStack(item, 1, meta));
        } else {
            return new MissingItemRenderer(WARNING_ITEM_MISSING);
        }
    }
}

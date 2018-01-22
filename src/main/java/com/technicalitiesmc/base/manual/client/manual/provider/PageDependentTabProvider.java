package com.technicalitiesmc.base.manual.client.manual.provider;

import com.technicalitiesmc.base.manual.api.manual.TabIconRenderer;
import com.technicalitiesmc.base.manual.api.manual.TabProvider;
import com.technicalitiesmc.base.manual.common.api.ManualAPIImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

public class PageDependentTabProvider implements TabProvider {
    private final TabIconRenderer renderer = new PageDependentTextureTabIconRenderer();
    private final Function<String, ResourceLocation> texture;
    private final Function<String, String> tooltip;
    private final Function<String, String> path;

    public PageDependentTabProvider(Function<String, ResourceLocation> texture, Function<String, String> tooltip, Function<String, String> path) {
        this.texture = texture;
        this.tooltip = tooltip;
        this.path = path;
    }

    @Nonnull
    @Override
    public TabIconRenderer getRenderer() {
        return renderer;
    }

    @Nullable
    @Override
    public String getTooltip() {
        return tooltip.apply(ManualAPIImpl.peekPath());
    }

    @Nullable
    @Override
    public String getPath() {
        return path.apply(ManualAPIImpl.peekPath());
    }

    private class PageDependentTextureTabIconRenderer implements TabIconRenderer {
        @Override
        @SideOnly(Side.CLIENT)
        public void render() {
            Minecraft.getMinecraft().getTextureManager().bindTexture(texture.apply(ManualAPIImpl.peekPath()));
            final Tessellator t = Tessellator.getInstance();
            final BufferBuilder b = t.getBuffer();
            b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            b.pos(0, 16, 0).tex(0, 1).endVertex();
            b.pos(16, 16, 0).tex(1, 1).endVertex();
            b.pos(16, 0, 0).tex(1, 0).endVertex();
            b.pos(0, 0, 0).tex(0, 0).endVertex();
            t.draw();
        }
    }
}

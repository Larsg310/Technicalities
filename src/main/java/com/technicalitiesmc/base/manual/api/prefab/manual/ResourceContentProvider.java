package com.technicalitiesmc.base.manual.api.prefab.manual;

import com.google.common.base.Charsets;
import com.technicalitiesmc.base.manual.api.manual.ContentProvider;
import com.technicalitiesmc.base.manual.common.api.ManualAPIImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Basic implementation of a content provider based on Minecraft's resource
 * loading framework.
 * <p>
 * Beware that the manual is unaware of resource domains. In other words, two
 * paths that are identical except for their resource domain will be the same,
 * as seen from the manual. This means you should probably place your
 * documentation somewhere other than <tt>doc/</tt>, because that's where the
 * RTFM documentation lives, and it is queried first - meaning if you
 * have a page with the same path as one in RTFM, it is practically
 * unreachable (because the OC provider is always queried first).
 */
@SuppressWarnings("UnusedDeclaration")
public class ResourceContentProvider implements ContentProvider {
    private final String resourceDomain;
    private final String basePath;
    private final boolean tldr;

    public ResourceContentProvider(final String resourceDomain, final String basePath, final boolean tldr) {
        this.resourceDomain = resourceDomain;
        this.basePath = basePath;
        this.tldr = tldr;
    }

    public ResourceContentProvider(final String resourceDomain, final boolean tldr) {
        this(resourceDomain, "", tldr);
    }

    @Override
    @Nullable
    public Iterable<String> getContent(final String path) {
        if (ManualAPIImpl.isTLDRMode() ^ tldr) return null;

        final ResourceLocation location = new ResourceLocation(resourceDomain, basePath + (path.startsWith("/") ? path.substring(1) : path));
        InputStream is = null;
        try {
            is = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
            final ArrayList<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        } catch (final Throwable ignored) {
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (final IOException ignored) { }
            }
        }
    }
}

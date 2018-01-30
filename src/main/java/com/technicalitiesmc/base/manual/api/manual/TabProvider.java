package com.technicalitiesmc.base.manual.api.manual;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface TabProvider {
    @Nonnull
    TabIconRenderer getRenderer();

    @Nullable
    String getTooltip();

    @Nullable
    String getPath();
}

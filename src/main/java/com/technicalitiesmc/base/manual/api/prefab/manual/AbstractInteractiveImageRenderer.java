package com.technicalitiesmc.base.manual.api.prefab.manual;

import com.technicalitiesmc.base.manual.api.manual.InteractiveImageRenderer;

/**
 * Simple base implementation of {@link com.technicalitiesmc.base.manual.api.manual.InteractiveImageRenderer}.
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class AbstractInteractiveImageRenderer implements InteractiveImageRenderer {
    @Override
    public String getTooltip(final String tooltip) {
        return tooltip;
    }

    @Override
    public boolean onMouseClick(final int mouseX, final int mouseY) {
        return false;
    }
}

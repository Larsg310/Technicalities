package com.technicalitiesmc.lib.util;

import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// made this but didn't need it in the end, leaving this since someone may need it in the future
// - Farfetchd
public enum RelativeFacing {
    FRONT(1, 2, 3),
    BACK(0, 3, 2),
    LEFT(3, 0, 1),
    RIGHT(2, 1, 0);

    private int opposite;
    private int left;
    private int right;

    RelativeFacing(int opposite, int left, int right) {
        this.opposite = opposite;
        this.left = left;
        this.right = right;
    }

    public RelativeFacing opposite() {
        return VALUES.get(opposite);
    }

    public RelativeFacing left() {
        return VALUES.get(left);
    }

    public RelativeFacing right() {
        return VALUES.get(right);
    }

    public EnumFacing getRelativeTo(EnumFacing orientation) {
        if (orientation.getAxis().isVertical()) return orientation;
        switch (this) {
            case FRONT:
                return orientation;
            case BACK:
                return orientation.getOpposite();
            case LEFT:
                return orientation.rotateYCCW();
            case RIGHT:
                return orientation.rotateY();
        }
        throw new IllegalStateException(); // wat?
    }

    public static final List<RelativeFacing> VALUES = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(values())));
}

package org.lodder.subtools.multisubdownloader.gui.extra;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class BoxModelProperties {
    @val @Nullable Integer top;
    @val @Nullable Integer left;
    @val @Nullable Integer bottom;
    @val @Nullable Integer right;

    public BoxModelProperties(@Nullable Integer top=null, @Nullable Integer left=null, @Nullable Integer bottom=null,
        @Nullable Integer right=null) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    public BoxModelProperties(int value) {
        this(value, value, value, value);
    }

    public String getInsets() {
        return "insets %s %s %s %s".formatted(getValue(top), getValue(left), getValue(bottom), getValue(right));
    }

    private String getValue(@Nullable Integer padding) {
        return padding == null ? "n" : String.valueOf(padding);
    }
}


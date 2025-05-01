package org.lodder.subtools.multisubdownloader.gui.extra;

import manifold.ext.props.rt.api.val;

public class BoxModelProperties {
    @val Integer top;
    @val Integer left;
    @val Integer bottom;
    @val Integer right;

    public BoxModelProperties(Integer top=null, Integer left=null, Integer bottom=null, Integer right=null) {
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

    private String getValue(Integer padding) {
        return padding == null ? "n" : String.valueOf(padding);
    }
}

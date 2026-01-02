package extensions.java.awt.GridBagConstraints;

import java.awt.*;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;
import org.jspecify.annotations.NullMarked;

@Extension
@NullMarked
public class GridBagConstraintsExt {
    private GridBagConstraintsExt() {
        // hide utility class constructor
    }

    public static @Self GridBagConstraints insets(@This GridBagConstraints gridBagConstraints, Insets insets) {
        gridBagConstraints.insets = insets;
        return gridBagConstraints;
    }

    public static @Self GridBagConstraints fill(@This GridBagConstraints gridBagConstraints, int fill) {
        gridBagConstraints.fill = fill;
        return gridBagConstraints;
    }

    public static @Self GridBagConstraints gridx(@This GridBagConstraints gridBagConstraints, int gridx) {
        gridBagConstraints.gridx = gridx;
        return gridBagConstraints;
    }

    public static @Self GridBagConstraints gridy(@This GridBagConstraints gridBagConstraints, int gridy) {
        gridBagConstraints.gridy = gridy;
        return gridBagConstraints;
    }
}

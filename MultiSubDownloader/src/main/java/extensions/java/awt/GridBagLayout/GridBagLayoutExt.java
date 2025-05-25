package extensions.java.awt.GridBagLayout;

import java.awt.*;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;

@Extension
public class GridBagLayoutExt {

    private GridBagLayoutExt() {
        // hide utility class constructor
    }


    public static @Self GridBagLayout columnWidths(@This GridBagLayout gridBagLayout, int[] columnWidths) {
        gridBagLayout.columnWidths = columnWidths;
        return gridBagLayout;
    }

    public static @Self GridBagLayout rowHeights(@This GridBagLayout gridBagLayout, int[] rowHeights) {
        gridBagLayout.rowHeights = rowHeights;
        return gridBagLayout;
    }

    public static @Self GridBagLayout columnWeights(@This GridBagLayout gridBagLayout, double[] columnWeights) {
        gridBagLayout.columnWeights = columnWeights;
        return gridBagLayout;
    }

    public static @Self GridBagLayout rowWeights(@This GridBagLayout gridBagLayout, double[] rowWeights) {
        gridBagLayout.rowWeights = rowWeights;
        return gridBagLayout;
    }
}

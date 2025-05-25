package extensions.javax.swing.JScrollPane;

import javax.swing.*;
import java.awt.*;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;

@Extension
public class JScrollPaneExt {

    private JScrollPaneExt() {
        // hide utility class constructor
    }

    public static @Self JScrollPane viewportView(@This JScrollPane scrollPane, Component view) {
        scrollPane.setViewportView(view);
        return scrollPane;
    }

}

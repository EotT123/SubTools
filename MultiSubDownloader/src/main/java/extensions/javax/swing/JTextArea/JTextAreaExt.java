package extensions.javax.swing.JTextArea;

import javax.swing.*;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;

@Extension
public class JTextAreaExt {

    private JTextAreaExt() {
        // hide utility class constructor
    }

    public static @Self JTextArea autoScrolls(@This JTextArea textArea, boolean autoScrolls) {
        textArea.setAutoscrolls(autoScrolls);
        return textArea;
    }
}

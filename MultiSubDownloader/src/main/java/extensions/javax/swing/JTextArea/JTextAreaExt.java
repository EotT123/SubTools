package extensions.javax.swing.JTextArea;

import javax.swing.*;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;
import org.jspecify.annotations.NullMarked;

@Extension
@NullMarked
public class JTextAreaExt {

    private JTextAreaExt() {
        // Hide Utility Class Constructor
    }

    public static @Self JTextArea autoScrolls(@This JTextArea textArea, boolean autoScrolls) {
        textArea.setAutoscrolls(autoScrolls);
        return textArea;
    }
}

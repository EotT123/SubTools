package extensions.javax.swing.JTextField;

import javax.swing.*;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;
import org.jspecify.annotations.NullMarked;

@Extension
@NullMarked
public class JTextFieldExt {

    private JTextFieldExt() {
        // Hide Utility Class Constructor
    }

    public static @Self JTextField columns(@This JTextField textField, int columns) {
        textField.setColumns(columns);
        return textField;
    }
}

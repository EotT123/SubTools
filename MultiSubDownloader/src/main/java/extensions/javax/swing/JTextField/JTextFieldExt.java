package extensions.javax.swing.JTextField;

import javax.swing.*;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;

@Extension
public class JTextFieldExt {

    private JTextFieldExt() {
        // hide utility class constructor
    }

    public static @Self JTextField columns(@This JTextField textField, int columns) {
        textField.setColumns(columns);
        return textField;
    }
}

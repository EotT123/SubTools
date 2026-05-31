package extensions.javax.swing.JButton;

import javax.swing.*;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;
import org.jspecify.annotations.NullMarked;

@Extension
@NullMarked
public class JButtonExt {

    private JButtonExt() {
        // Hide Utility Class Constructor
    }

    public static @Self JButton defaultButtonFor(@This JButton abstractButton, JRootPane rootPane) {
        rootPane.setDefaultButton(abstractButton);
        return abstractButton;
    }
}

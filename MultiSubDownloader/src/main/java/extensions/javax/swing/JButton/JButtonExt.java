package extensions.javax.swing.JButton;

import javax.swing.*;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;

@Extension
public class JButtonExt {

    private JButtonExt() {
        // hide utility class constructor
    }

    public static @Self JButton defaultButtonFor(@This JButton abstractButton, JRootPane rootPane) {
        rootPane.setDefaultButton(abstractButton);
        return abstractButton;
    }
}

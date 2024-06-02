package extensions.javax.swing.JTextArea;

import javax.swing.*;

import lombok.experimental.UtilityClass;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;

@UtilityClass
@Extension
public class JTextAreaExt {

    public static @Self JTextArea autoscrolls(@This JTextArea textArea, boolean autoscrolls) {
        textArea.setAutoscrolls(autoscrolls);
        return textArea;
    }
}

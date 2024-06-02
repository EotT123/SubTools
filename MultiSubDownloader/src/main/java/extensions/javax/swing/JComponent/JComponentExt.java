package extensions.javax.swing.JComponent;

import javax.swing.*;
import java.awt.*;

import extensions.java.awt.Component.ComponentExt;
import lombok.experimental.UtilityClass;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;

@UtilityClass
@Extension
public class JComponentExt {

    public static @Self JComponent withEnabled(@This JComponent component, boolean enabled) {
        component.setEnabled(enabled);
        return component;
    }

    public static @Self JComponent withEnabled(@This JComponent component) {
        withEnabled(component, true);
        return component;
    }

    public static @Self JComponent withDisabled(@This JComponent component) {
        withEnabled(component, false);
        return component;
    }

    public static <S extends Container> @Self JComponent addTo(@This JComponent child, S parent) {
        parent.add(child);
        return child;
    }

    public static <S extends Container> @Self JComponent addTo(@This JComponent child, S parent, Object constraints) {
        parent.add(child, constraints);
        return child;
    }

    public static void setEnabledRecursive(@This JComponent component, boolean enabled) {
        ComponentExt.setRecursive(component, c -> c.setEnabled(enabled));
    }

    public static @Self JComponent enabledRecursive(@This JComponent component, boolean enabled) {
        setEnabledRecursive(component, enabled);
        return component;
    }

    public static @Self JComponent withToolTipText(@This JComponent component, String text) {
        component.setToolTipText(text);
        return component;
    }
}

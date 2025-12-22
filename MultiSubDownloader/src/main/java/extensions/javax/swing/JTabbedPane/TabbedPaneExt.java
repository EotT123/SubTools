package extensions.javax.swing.JTabbedPane;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;
import org.jspecify.annotations.NullMarked;

@Extension
@NullMarked
public class TabbedPaneExt {

    private TabbedPaneExt() {
        // hide utility class constructor
    }

    public static @Self JTabbedPane tabLayoutPolicy(@This JTabbedPane tabbedPane, int tabLayoutPolicy) {
        tabbedPane.tabLayoutPolicy = tabLayoutPolicy;
        return tabbedPane;
    }

    //    public static @Self JTabbedPane changeListener(@This JTabbedPane tabbedPane, ChangeListener changeListener) {
    //        tabbedPane.addChangeListener(changeListener);
    //        return tabbedPane;
    //    }

    public static @Self JTabbedPane changeListener(@This JTabbedPane tabbedPane,
        Consumer<JTabbedPane> changeListener) {
        tabbedPane.addChangeListener(_ -> changeListener.accept(tabbedPane));
        return tabbedPane;
    }

    public static @Self JTabbedPane withTab(@This JTabbedPane tabbedPane,
        String title, Component component) {
        tabbedPane.addTab(title, null, component, null);
        return tabbedPane;
    }

    public static @Self JTabbedPane withTab(@This JTabbedPane tabbedPane,
        String title, Icon icon, Component component, String tip) {
        tabbedPane.addTab(title, icon, component, tip);
        return tabbedPane;
    }
}

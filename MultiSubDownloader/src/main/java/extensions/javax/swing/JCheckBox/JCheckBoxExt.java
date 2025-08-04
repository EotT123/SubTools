package extensions.javax.swing.JCheckBox;

import javax.swing.*;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;
import org.lodder.subtools.sublibrary.util.function.BooleanConsumer;

@Extension
public class JCheckBoxExt {
    private JCheckBoxExt() {
        // hide utility class constructor
    }

    public static @Self JCheckBox addCheckedChangeListener(@This JCheckBox checkBox, BooleanConsumer... listeners) {
        checkBox.addItemListener(e -> listeners.forEach(lis -> lis.accept(((JCheckBox) e.getSource()).isSelected())));
        return checkBox;
    }

    public static @Self JCheckBox visible(@This JCheckBox checkBox, boolean visible) {
        checkBox.setVisible(visible);
        return checkBox;
    }

    public static @Self JCheckBox selected(@This JCheckBox checkBox, boolean selected) {
        checkBox.setSelected(selected);
        return checkBox;
    }
}

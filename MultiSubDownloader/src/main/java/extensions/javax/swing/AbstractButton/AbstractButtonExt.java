package extensions.javax.swing.AbstractButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.util.function.BooleanConsumer;

@Extension
@NullMarked
public class AbstractButtonExt {
    private AbstractButtonExt() {
        // Hide Utility Class Constructor
    }

    public static @Self AbstractButton actionListener(@This AbstractButton abstractButton, ActionListener listener) {
        abstractButton.addActionListener(listener);
        return abstractButton;
    }

    public static @Self AbstractButton actionListener(@This AbstractButton abstractButton, Runnable listener) {
        return abstractButton.actionListener(_ -> listener.run());
    }

    public static @Self AbstractButton actionListenerSelf(@This AbstractButton abstractButton,
            Consumer<AbstractButton> selfConsumerListener) {
        return abstractButton.actionListener(_ -> selfConsumerListener.accept(abstractButton));
    }

    public static @Self AbstractButton selectedListener(@This AbstractButton abstractButton,
            BooleanConsumer selectedConsumer) {
        return abstractButton.actionListener(_ -> selectedConsumer.accept(abstractButton.isSelected()));
    }

    public static @Self AbstractButton actionCommand(@This AbstractButton abstractButton, String actionCommand) {
        abstractButton.setActionCommand(actionCommand);
        return abstractButton;
    }

    public static @Self AbstractButton withMargin(@This AbstractButton abstractButton, Insets insets) {
        abstractButton.setMargin(insets);
        return abstractButton;
    }
}

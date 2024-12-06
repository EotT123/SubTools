package extensions.javax.swing.AbstractButton;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

import lombok.experimental.UtilityClass;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;
import org.lodder.subtools.sublibrary.util.BooleanConsumer;

@UtilityClass
@Extension
public class AbstractButtonExt {

    public static @Self AbstractButton withActionListener(@This AbstractButton abstractButton, ActionListener listener) {
        abstractButton.addActionListener(listener);
        return abstractButton;
    }

    public static @Self AbstractButton withActionListener(@This AbstractButton abstractButton, Runnable listener) {
        withActionListener(abstractButton, _ -> listener.run());
        return abstractButton;
    }

    public static @Self AbstractButton withActionListenerSelf(@This AbstractButton abstractButton,
            Consumer<@Self AbstractButton> selfConsumerListener) {
        withActionListener(abstractButton, _ -> selfConsumerListener.accept(abstractButton));
        return abstractButton;
    }

    public static @Self AbstractButton withSelectedListener(@This AbstractButton abstractButton, BooleanConsumer selectedConsumer) {
        withActionListener(abstractButton, _ -> selectedConsumer.accept(abstractButton.isSelected()));
        return abstractButton;
    }

    public static @Self AbstractButton actionCommand(@This AbstractButton abstractButton, String actionCommand) {
        abstractButton.setActionCommand(actionCommand);
        return abstractButton;
    }

    public static @Self AbstractButton withActionCommand(@This AbstractButton abstractButton, String actionCommand) {
        abstractButton.getModel().setActionCommand(actionCommand);
        return abstractButton;
    }
}

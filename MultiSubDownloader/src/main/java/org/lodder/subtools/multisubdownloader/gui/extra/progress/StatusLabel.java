package org.lodder.subtools.multisubdownloader.gui.extra.progress;

import javax.swing.*;
import java.io.Serial;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class StatusLabel extends JLabel implements Messenger {

    @Serial
    private static final long serialVersionUID = 1L;

    public StatusLabel(String string) {
        super(string);
    }

    @Override
    public void message(String message) {
        setText(message);
    }

}

package org.lodder.subtools.multisubdownloader.gui.dialog;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Cancelable {
    boolean cancel(boolean mayInterruptIfRunning);
}

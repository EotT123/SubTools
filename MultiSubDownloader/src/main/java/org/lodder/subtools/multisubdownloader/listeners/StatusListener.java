package org.lodder.subtools.multisubdownloader.listeners;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.actions.ActionException;

@NullMarked
public interface StatusListener {

    void onError(ActionException exception);

    void onStatus(String message);

}

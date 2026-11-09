package org.lodder.subtools.multisubdownloader.listener;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.actions.ActionException;

@NullMarked
public interface StatusListener {

    void onError(ActionException exception);

    void onStatus(String message);

}

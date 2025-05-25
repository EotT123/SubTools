package org.lodder.subtools.multisubdownloader.actions;

import java.io.Serial;

public class ActionException extends Exception {

    @Serial
    private static final long serialVersionUID = -7453153452045851404L;

    public ActionException(String message) {
        super(message);
    }

    protected ActionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

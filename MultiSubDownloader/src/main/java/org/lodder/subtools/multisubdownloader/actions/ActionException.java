package org.lodder.subtools.multisubdownloader.actions;

import java.io.Serial;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class ActionException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public ActionException(String message) {
        super(message);
    }

    protected ActionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

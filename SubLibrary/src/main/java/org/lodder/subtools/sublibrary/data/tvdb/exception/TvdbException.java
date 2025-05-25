package org.lodder.subtools.sublibrary.data.tvdb.exception;

import java.io.Serial;

public class TvdbException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public TvdbException(String message) {
        super(message);
    }

    public TvdbException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

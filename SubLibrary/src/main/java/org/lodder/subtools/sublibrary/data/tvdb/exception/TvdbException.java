package org.lodder.subtools.sublibrary.data.tvdb.exception;

import java.io.Serial;

import lombok.experimental.StandardException;

@StandardException
public class TvdbException extends Exception {

    @Serial
    private static final long serialVersionUID = 230737234160207201L;

    protected TvdbException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

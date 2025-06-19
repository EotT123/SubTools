package org.lodder.subtools.sublibrary.data.imdb.exception;

import java.io.Serial;

public class ImdbException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public ImdbException(String s) {
        super(s);
    }

    public ImdbException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImdbException(Throwable cause) {
        super(cause);
    }

}

package org.lodder.subtools.sublibrary.data.omdb.exception;

import java.io.Serial;

public class OmdbException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public OmdbException(String message, Throwable cause) {
        super(message, cause);
    }
}

package org.lodder.subtools.sublibrary.data.omdb.exception;

import java.io.Serial;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class OmdbException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public OmdbException(String message) {
        super(message);
    }

    public OmdbException(String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    public OmdbException(Throwable cause) {
        super(cause);
    }
}

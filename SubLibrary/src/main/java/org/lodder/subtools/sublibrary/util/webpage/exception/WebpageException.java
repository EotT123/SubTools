package org.lodder.subtools.sublibrary.util.webpage.exception;

import java.io.IOException;
import java.io.Serial;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class WebpageException extends IOException {
    @Serial
    private static final long serialVersionUID = 1L;

    public WebpageException(@Nullable Throwable cause=null) {
        super(null, cause);
    }

    public WebpageException(@Nullable String message, @Nullable Throwable cause=null) {
        super(message, cause);
    }
}

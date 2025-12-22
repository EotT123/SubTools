package org.lodder.subtools.sublibrary.exception;

import java.io.IOException;
import java.io.Serial;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class WebpageException extends IOException {
    @Serial
    private static final long serialVersionUID = 1L;

    public WebpageException(String message) {
        super(message);
    }
}

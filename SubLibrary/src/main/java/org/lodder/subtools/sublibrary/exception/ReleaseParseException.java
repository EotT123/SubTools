package org.lodder.subtools.sublibrary.exception;

import java.io.Serial;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class ReleaseParseException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public ReleaseParseException(String exception) {
        super(exception);
    }
}

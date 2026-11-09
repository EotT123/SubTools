package org.lodder.subtools.multisubdownloader.exception;

import java.io.Serial;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class CliException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public CliException(String message) {
        super(message);
    }
}

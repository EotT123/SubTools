package org.lodder.subtools.sublibrary.exception;

import java.io.Serial;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class ControlFactoryException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public ControlFactoryException(String string) {
        super(string);
    }
}

package org.lodder.subtools.sublibrary.exception;

import java.io.Serial;

public class ControlFactoryException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public ControlFactoryException(String string) {
        super(string);
    }
}

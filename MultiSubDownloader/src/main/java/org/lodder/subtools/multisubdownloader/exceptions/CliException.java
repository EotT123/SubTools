package org.lodder.subtools.multisubdownloader.exceptions;

import java.io.Serial;

public class CliException extends Exception {

    @Serial
    private static final long serialVersionUID = -9050358290926245586L;

    public CliException(String message) {
        super(message);
    }
}

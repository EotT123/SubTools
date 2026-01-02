package org.lodder.subtools.sublibrary.exception;

import java.io.Serial;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.model.ReleaseWithoutPath;

@NullMarked
public class ReleaseControlException extends Exception {

    public ReleaseControlException(String string, ReleaseWithoutPath release) {
        super(string + ": " + release);
    }

    @Serial
    private static final long serialVersionUID = 1L;

}

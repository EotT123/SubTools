package org.lodder.subtools.multisubdownloader.exceptions;

import java.io.Serial;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.actions.ActionException;

@NullMarked
public class SearchSetupException extends ActionException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SearchSetupException(String message) {
        super(message);
    }
}

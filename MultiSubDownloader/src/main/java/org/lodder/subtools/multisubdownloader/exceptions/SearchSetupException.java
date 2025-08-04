package org.lodder.subtools.multisubdownloader.exceptions;

import java.io.Serial;

import org.lodder.subtools.multisubdownloader.actions.ActionException;

public class SearchSetupException extends ActionException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SearchSetupException(String message) {
        super(message);
    }

    protected SearchSetupException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

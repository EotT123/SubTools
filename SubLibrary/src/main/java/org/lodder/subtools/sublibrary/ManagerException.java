package org.lodder.subtools.sublibrary;

import java.io.Serial;

public class ManagerException extends Exception {

    @Serial
    private static final long serialVersionUID = -7937716379280570736L;

    public ManagerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ManagerException(Throwable cause) {
        super(cause);
    }
    
    public ManagerException(String message, Throwable cause) {
        super(message, cause);
    }
}

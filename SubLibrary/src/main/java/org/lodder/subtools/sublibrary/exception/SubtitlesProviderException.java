package org.lodder.subtools.sublibrary.exception;

import java.io.Serial;

import manifold.ext.props.rt.api.val;

public abstract class SubtitlesProviderException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    @val abstract String subtitleProvider;

    protected SubtitlesProviderException(String message) {
        super(message);
    }

    protected SubtitlesProviderException(Throwable cause) {
        super(cause);
    }

    protected SubtitlesProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}

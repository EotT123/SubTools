package org.lodder.subtools.sublibrary.exception;

import java.io.Serial;

import manifold.ext.props.rt.api.val;

public abstract class SubtitlesProviderException extends Exception {

    @Serial
    private static final long serialVersionUID = -2959483164333075297L;

    @val abstract String subtitleProvider;

    protected SubtitlesProviderException(String message) {
        super(message);
    }

    protected SubtitlesProviderException(Throwable cause) {
        super(cause);
    }
}

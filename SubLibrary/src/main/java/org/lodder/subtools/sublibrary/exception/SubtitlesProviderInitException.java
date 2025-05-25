package org.lodder.subtools.sublibrary.exception;

import java.io.Serial;

import manifold.ext.props.rt.api.val;

public class SubtitlesProviderInitException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;
    
    @val String providerName;

    public SubtitlesProviderInitException(String providerName, Throwable e) {
        super(e);
        this.providerName = providerName;
    }

    public SubtitlesProviderInitException(String providerName, String message) {
        super(message);
        this.providerName = providerName;
    }
}

package org.lodder.subtools.sublibrary.exception;

import java.io.Serial;

import lombok.Getter;

public class SubtitlesProviderInitException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -2959483164333075297L;
    @Getter
    private final String providerName;

    public SubtitlesProviderInitException(String providerName, Throwable e) {
        super(e);
        this.providerName = providerName;
    }

    public SubtitlesProviderInitException(String providerName, String message) {
        super(message);
        this.providerName = providerName;
    }
}

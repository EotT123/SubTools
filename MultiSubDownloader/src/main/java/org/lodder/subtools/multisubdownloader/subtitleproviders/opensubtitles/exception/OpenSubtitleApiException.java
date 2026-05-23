package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.exception;

import static org.lodder.subtools.sublibrary.CacheStrategy.*;
import static org.lodder.subtools.sublibrary.LogLevel.*;
import static org.lodder.subtools.sublibrary.util.webpage.http.HttpStatus.*;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.CacheStrategy;
import org.lodder.subtools.sublibrary.LogLevel;
import org.lodder.subtools.sublibrary.util.webpage.http.ApiExceptionIntf;
import org.lodder.subtools.sublibrary.util.webpage.http.HttpStatus;

@NullMarked
public class OpenSubtitleApiException extends OpenSubtitleException implements ApiExceptionIntf {

    @val @override HttpStatus errorCode;
    @val @override CacheStrategy cacheStrategy;
    @val @override LogLevel logLevel;
    @val boolean stopContactingServer;

    public OpenSubtitleApiException(HttpStatus errorCode, String errorMessage, CacheStrategy cacheStrategy,
        LogLevel logLevel) {
        this(errorCode, errorMessage, cacheStrategy, logLevel, false);
    }

    private OpenSubtitleApiException(HttpStatus errorCode, String errorMessage, CacheStrategy cacheStrategy,
        LogLevel logLevel, boolean stopContactingServer) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.cacheStrategy = cacheStrategy;
        this.logLevel = logLevel;
        this.stopContactingServer = stopContactingServer;
    }

    private OpenSubtitleApiException(HttpStatus errorCode, Exception cause, String message, CacheStrategy cacheStrategy,
        LogLevel logLevel, boolean stopContactingServer) {
        super(message, cause);
        this.errorCode = errorCode;
        this.cacheStrategy = cacheStrategy;
        this.logLevel = logLevel;
        this.stopContactingServer = stopContactingServer;
    }

    public static OpenSubtitleApiException noResult(String message, CacheStrategy cacheStrategy=CACHE_TEMPORARY,
        LogLevel logLevel=WARN) {
        return new OpenSubtitleApiException(NO_CONTENT, message, cacheStrategy, logLevel);
    }

    public static OpenSubtitleApiException error(Exception cause, String message=cause.getMessage(),
        CacheStrategy cacheStrategy=CACHE_TEMPORARY) {
        return new OpenSubtitleApiException(SERVER_ERROR, cause, message, cacheStrategy, ERROR, false);
    }

    public static OpenSubtitleApiException stopContactingServer(String message,
        CacheStrategy cacheStrategy=CACHE_DISABLED, LogLevel logLevel=WARN) {
        return new OpenSubtitleApiException(SERVER_ERROR, message, cacheStrategy, logLevel, true);
    }
}

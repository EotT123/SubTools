package org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.exception;

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
public class Addic7edApiException extends Addic7edException implements ApiExceptionIntf {

    @val @override HttpStatus errorCode;
    @val @override CacheStrategy cacheStrategy;
    @val @override LogLevel logLevel;

    public Addic7edApiException(HttpStatus errorCode, String errorMessage, CacheStrategy cacheStrategy,
        LogLevel logLevel) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.cacheStrategy = cacheStrategy;
        this.logLevel = logLevel;
    }

    private Addic7edApiException(HttpStatus errorCode, Exception cause, String message, CacheStrategy cacheStrategy,
        LogLevel logLevel) {
        super(message, cause);
        this.errorCode = errorCode;
        this.cacheStrategy = cacheStrategy;
        this.logLevel = logLevel;
    }

    public static Addic7edApiException noResult(String message, CacheStrategy cacheStrategy=CACHE_TEMPORARY,
        LogLevel logLevel=WARN) {
        return new Addic7edApiException(NO_CONTENT, message, cacheStrategy, logLevel);
    }

    public static Addic7edApiException error(Exception cause, String message=cause.getMessage(),
        CacheStrategy cacheStrategy=CACHE_TEMPORARY) {
        return new Addic7edApiException(SERVER_ERROR, cause, message, cacheStrategy, ERROR);
    }
}

package org.lodder.subtools.multisubdownloader.subtitleprovider.podnapisi.exception;

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
public class PodnapisiApiException extends PodnapisiException implements ApiExceptionIntf {

    @val @override HttpStatus errorCode;
    @val @override CacheStrategy cacheStrategy;
    @val @override LogLevel logLevel;

    public PodnapisiApiException(HttpStatus errorCode, String errorMessage, CacheStrategy cacheStrategy,
        LogLevel logLevel) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.cacheStrategy = cacheStrategy;
        this.logLevel = logLevel;
    }

    private PodnapisiApiException(HttpStatus errorCode, Exception cause, String message=
        cause.getMessage(), CacheStrategy cacheStrategy, LogLevel logLevel) {
        super(message, cause);
        this.errorCode = errorCode;
        this.cacheStrategy = cacheStrategy;
        this.logLevel = logLevel;
    }

    public static PodnapisiApiException noResult(String message) {
        return new PodnapisiApiException(NO_CONTENT, message, CACHE_TEMPORARY, WARN);
    }

    public static PodnapisiApiException error(Exception cause, String message=cause.getMessage(),
        CacheStrategy cacheStrategy=CACHE_TEMPORARY) {
        return new PodnapisiApiException(SERVER_ERROR, cause, message, cacheStrategy, ERROR);
    }
}
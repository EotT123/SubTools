package org.lodder.subtools.sublibrary.data.imdb.exception;

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
public class ImdbApiException extends ImdbException implements ApiExceptionIntf {

    @val @override HttpStatus errorCode;
    @val @override CacheStrategy cacheStrategy;
    @val @override LogLevel logLevel;

    public ImdbApiException(HttpStatus errorCode, String errorMessage, CacheStrategy cacheStrategy,
        LogLevel logLevel) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.cacheStrategy = cacheStrategy;
        this.logLevel = logLevel;
    }

    private ImdbApiException(HttpStatus errorCode, Exception cause, String message, CacheStrategy cacheStrategy,
        LogLevel logLevel) {
        super(message, cause);
        this.errorCode = errorCode;
        this.cacheStrategy = cacheStrategy;
        this.logLevel = logLevel;
    }

    public static ImdbApiException noResult(String message, CacheStrategy cacheStrategy=CACHE_TEMPORARY,
        LogLevel logLevel=WARN) {
        return new ImdbApiException(NO_CONTENT, message, cacheStrategy, logLevel);
    }

    public static ImdbApiException error(Exception cause, String message=cause.getMessage(),
        CacheStrategy cacheStrategy=CACHE_TEMPORARY) {
        return new ImdbApiException(SERVER_ERROR, cause, message, cacheStrategy, ERROR);
    }
}

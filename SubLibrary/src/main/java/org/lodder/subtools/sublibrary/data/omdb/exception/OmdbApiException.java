package org.lodder.subtools.sublibrary.data.omdb.exception;

import static org.lodder.subtools.sublibrary.CacheStrategy.*;
import static org.lodder.subtools.sublibrary.LogLevel.*;
import static org.lodder.subtools.sublibrary.util.http.HttpStatus.*;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.CacheStrategy;
import org.lodder.subtools.sublibrary.LogLevel;
import org.lodder.subtools.sublibrary.util.http.ApiExceptionIntf;
import org.lodder.subtools.sublibrary.util.http.HttpStatus;

public class OmdbApiException extends OmdbException implements ApiExceptionIntf {

    @val @override HttpStatus errorCode;
    @val @override CacheStrategy cacheStrategy;
    @val @override LogLevel logLevel;

    public OmdbApiException(HttpStatus errorCode, String errorMessage, CacheStrategy cacheStrategy,
        LogLevel logLevel) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.cacheStrategy = cacheStrategy;
        this.logLevel = logLevel;
    }

    private OmdbApiException(HttpStatus errorCode, Exception cause, String message, CacheStrategy cacheStrategy,
        LogLevel logLevel) {
        super(message, cause);
        this.errorCode = errorCode;
        this.cacheStrategy = cacheStrategy;
        this.logLevel = logLevel;
    }

    public static OmdbApiException noResult(String message, CacheStrategy cacheStrategy=CACHE_TEMPORARY,
        LogLevel logLevel=WARN) {
        return new OmdbApiException(NO_CONTENT, message, cacheStrategy, logLevel);
    }

    public static OmdbApiException error(Exception cause, String message=cause.getMessage(),
        CacheStrategy cacheStrategy=CACHE_TEMPORARY) {
        return new OmdbApiException(SERVER_ERROR, cause, message, cacheStrategy, ERROR);
    }
}

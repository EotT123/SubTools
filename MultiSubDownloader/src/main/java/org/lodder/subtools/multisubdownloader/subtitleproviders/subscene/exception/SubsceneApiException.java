package org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.exception;

import static org.lodder.subtools.sublibrary.CacheStrategy.*;
import static org.lodder.subtools.sublibrary.LogLevel.*;
import static org.lodder.subtools.sublibrary.util.http.HttpStatus.*;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.CacheStrategy;
import org.lodder.subtools.sublibrary.LogLevel;
import org.lodder.subtools.sublibrary.util.http.ApiExceptionIntf;
import org.lodder.subtools.sublibrary.util.http.HttpStatus;

public class SubsceneApiException extends SubsceneException implements ApiExceptionIntf {

    @val @override HttpStatus errorCode;
    @val @override CacheStrategy cacheStrategy;
    @val @override LogLevel logLevel;

    public SubsceneApiException(HttpStatus errorCode, String errorMessage, CacheStrategy cacheStrategy,
        LogLevel logLevel) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.cacheStrategy = cacheStrategy;
        this.logLevel = logLevel;
    }

    private SubsceneApiException(HttpStatus errorCode, Exception cause, String message=
        cause.getMessage(), CacheStrategy cacheStrategy, LogLevel logLevel) {
        super(message, cause);
        this.errorCode = errorCode;
        this.cacheStrategy = cacheStrategy;
        this.logLevel = logLevel;
    }

    public static SubsceneApiException noResult(String message) {
        return new SubsceneApiException(NO_CONTENT, message, CACHE_TEMPORARY, WARN);
    }

    public static SubsceneApiException error(Exception cause=null, String message=cause == null ? null :
        cause.getMessage(), CacheStrategy cacheStrategy=CACHE_TEMPORARY) {
        return new SubsceneApiException(SERVER_ERROR, cause, message, cacheStrategy, ERROR);
    }

//    public static SubsceneApiException error(String message, CacheStrategy cacheStrategy=CACHE_TEMPORARY) {
//        return new SubsceneApiException(SERVER_ERROR, null, message, cacheStrategy, ERROR);
//    }
}

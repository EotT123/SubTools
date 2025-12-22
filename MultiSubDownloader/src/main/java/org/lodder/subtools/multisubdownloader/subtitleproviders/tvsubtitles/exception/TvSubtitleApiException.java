package org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles.exception;

import static org.lodder.subtools.sublibrary.CacheStrategy.*;
import static org.lodder.subtools.sublibrary.LogLevel.*;
import static org.lodder.subtools.sublibrary.util.http.HttpStatus.*;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.CacheStrategy;
import org.lodder.subtools.sublibrary.LogLevel;
import org.lodder.subtools.sublibrary.util.http.ApiExceptionIntf;
import org.lodder.subtools.sublibrary.util.http.HttpStatus;

@NullMarked
public class TvSubtitleApiException extends TvSubtitleException implements ApiExceptionIntf {

    @val @override HttpStatus errorCode;
    @val @override CacheStrategy cacheStrategy;
    @val @override LogLevel logLevel;

    public TvSubtitleApiException(HttpStatus errorCode, String errorMessage, CacheStrategy cacheStrategy,
        LogLevel logLevel) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.cacheStrategy = cacheStrategy;
        this.logLevel = logLevel;
    }

    private TvSubtitleApiException(HttpStatus errorCode, Exception cause, String message=
        cause.getMessage(), CacheStrategy cacheStrategy, LogLevel logLevel) {
        super(message, cause);
        this.errorCode = errorCode;
        this.cacheStrategy = cacheStrategy;
        this.logLevel = logLevel;
    }

    public static TvSubtitleApiException noResult(String message) {
        return new TvSubtitleApiException(NO_CONTENT, message, CACHE_TEMPORARY, WARN);
    }

    public static TvSubtitleApiException error(Exception cause, String message=cause.getMessage(),
        CacheStrategy cacheStrategy=CACHE_TEMPORARY) {
        return new TvSubtitleApiException(SERVER_ERROR, cause, message, cacheStrategy, ERROR);
    }
}

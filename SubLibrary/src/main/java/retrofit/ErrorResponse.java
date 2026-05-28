package retrofit;

import static org.lodder.subtools.sublibrary.CacheStrategy.*;
import static org.lodder.subtools.sublibrary.LogLevel.*;
import static util.Utils.*;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.CacheStrategy;
import org.lodder.subtools.sublibrary.LogLevel;
import org.lodder.subtools.sublibrary.util.webpage.http.HttpStatus;

@NullMarked
public final class ErrorResponse implements Response {

    @val HttpStatus code;
    @val String message;
    @val LogLevel logLevel;
    @val CacheStrategy cacheStrategy;

    public ErrorResponse(HttpStatus code, String message, LogLevel logLevel, CacheStrategy cacheStrategy) {
        this.code = code;
        this.message = message;
        this.logLevel = logLevel;
        this.cacheStrategy = cacheStrategy;
    }

    public ErrorResponse(HttpStatus code, String message) {
        this.code = code;
        this.message = message;
        ErrorHandlerType errorHandlerType = ErrorHandlerType.getForCode(code);
        this.logLevel = ifNotNullOrElse(errorHandlerType, e -> e.logLevel, ERROR);
        this.cacheStrategy = ifNullThen(ifNotNull(errorHandlerType, e -> e.cacheStrategy), CACHE_TEMPORARY);
    }
}

package connection;

import static manifold.science.measures.TimeUnit.*;
import static manifold.science.util.UnitConstants.*;
import static org.lodder.subtools.sublibrary.CacheStrategy.*;
import static org.lodder.subtools.sublibrary.LogLevel.*;
import static org.lodder.subtools.sublibrary.util.webpage.http.HttpStatus.*;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import manifold.ext.props.rt.api.val;
import manifold.science.measures.Time;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.CacheStrategy;
import org.lodder.subtools.sublibrary.LogLevel;
import org.lodder.subtools.sublibrary.util.webpage.http.HttpStatus;

@NullMarked
public
enum ErrorHandlerType {
    ERR_BAD_REQUEST(BAD_REQUEST),
    ERR_UNAUTHORIZED(UNAUTHORIZED, CACHE_DISABLED),
    ERR_PAYMENT_REQUIRED(PAYMENT_REQUIRED, CACHE_DISABLED),
    ERR_FORBIDDEN(FORBIDDEN),
    ERR_NOT_FOUND(NOT_FOUND, logLevel:INFO),
    ERR_METHOD_NOT_ALLOWED(METHOD_NOT_ALLOWED),
    ERR_NOT_ACCEPTABLE(NOT_ACCEPTABLE),
    ERR_PROXY_AUTHENTICATION_REQUIRED(PROXY_AUTHENTICATION_REQUIRED, CACHE_DISABLED),
    ERR_REQUEST_TIMEOUT(REQUEST_TIMEOUT, CACHE_DISABLED, sleepTimeBeforeRetry:5 s),
    ERR_CONFLICT(CONFLICT),
    ERR_GONE(GONE),
    ERR_LENGTH_REQUIRED(LENGTH_REQUIRED),
    ERR_PRECONDITION_FAILED(PRECONDITION_FAILED),
    ERR_REQUEST_TOO_LONG(REQUEST_TOO_LONG),
    ERR_REQUEST_URI_TOO_LONG(REQUEST_URI_TOO_LONG),
    ERR_UNSUPPORTED_MEDIA_TYPE(UNSUPPORTED_MEDIA_TYPE),
    ERR_REQUESTED_RANGE_NOT_SATISFIABLE(REQUESTED_RANGE_NOT_SATISFIABLE),
    ERR_EXPECTATION_FAILED(EXPECTATION_FAILED),
    ERR_INSUFFICIENT_SPACE_ON_RESOURCE(INSUFFICIENT_SPACE_ON_RESOURCE),
    ERR_METHOD_FAILURE(METHOD_FAILURE, sleepTimeBeforeRetry:2 Second),
    ERR_MISDIRECTED_REQUEST(MISDIRECTED_REQUEST),
    ERR_UNPROCESSABLE_ENTITY(UNPROCESSABLE_ENTITY),
    ERR_LOCKED(LOCKED, logLevel:INFO, sleepTimeBeforeRetry:5 Second),
    ERR_TOO_EARLY(TOO_EARLY, sleepTimeBeforeRetry:1 Second),
    ERR_UPGRADE_REQUIRED(UPGRADE_REQUIRED),
    ERR_PRECONDITION_REQUIRED(PRECONDITION_REQUIRED),
    ERR_TOO_MANY_REQUESTS(TOO_MANY_REQUESTS, CACHE_DISABLED, WARN, 5 Second),
    ERR_REQUEST_HEADER_FIELDS_TOO_LARGE(REQUEST_HEADER_FIELDS_TOO_LARGE),
    ERR_UNAVAILABLE_FOR_LEGAL_REASONS(UNAVAILABLE_FOR_LEGAL_REASONS),

    // 5xx Server Error
    ERR_SERVER_ERROR(SERVER_ERROR, sleepTimeBeforeRetry:2 Second),
    ERR_NOT_IMPLEMENTED(NOT_IMPLEMENTED),
    ERR_BAD_GATEWAY(BAD_GATEWAY, sleepTimeBeforeRetry:2 Second),
    ERR_SERVICE_UNAVAILABLE(SERVICE_UNAVAILABLE, sleepTimeBeforeRetry:5 Second),
    ERR_GATEWAY_TIMEOUT(GATEWAY_TIMEOUT, sleepTimeBeforeRetry:2 Second),
    ERR_HTTP_VERSION_NOT_SUPPORTED(HTTP_VERSION_NOT_SUPPORTED),
    ERR_VARIANT_ALSO_NEGOTIATES(VARIANT_ALSO_NEGOTIATES),
    ERR_INSUFFICIENT_STORAGE(INSUFFICIENT_STORAGE),
    ERR_LOOP_DETECTED(LOOP_DETECTED),
    ERR_NOT_EXTENDED(NOT_EXTENDED),
    ERR_NETWORK_AUTHENTICATION_REQUIRED(NETWORK_AUTHENTICATION_REQUIRED);

    @val HttpStatus code;
    @val CacheStrategy cacheStrategy;
    @val LogLevel logLevel;
    @val @Nullable Time sleepTimeBeforeRetry;

    private static Map<Integer, ErrorHandlerType> MAP =
        Arrays.stream(values()).collect(Collectors.toMap(eht -> eht.code.code, Function.identity()));

    ErrorHandlerType(HttpStatus code, CacheStrategy cacheStrategy=CACHE_TEMPORARY, LogLevel logLevel=ERROR,
        @Nullable Time sleepTimeBeforeRetry=null) {
        this.code = code;
        this.cacheStrategy = cacheStrategy;
        this.logLevel = logLevel;
        this.sleepTimeBeforeRetry = sleepTimeBeforeRetry;
    }

    public static @Nullable ErrorHandlerType getForCode(int code) {
        return MAP.get(code);
    }

    public static @Nullable ErrorHandlerType getForCode(HttpStatus code) {
        return MAP.get(code.code);
    }

}

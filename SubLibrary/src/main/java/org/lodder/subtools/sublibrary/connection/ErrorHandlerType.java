package org.lodder.subtools.sublibrary.connection;

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
    ERR_BAD_REQUEST(new CustomErrorHandler(BAD_REQUEST)),
    ERR_UNAUTHORIZED(new CustomErrorHandler(UNAUTHORIZED, CACHE_DISABLED)),
    ERR_PAYMENT_REQUIRED(new CustomErrorHandler(PAYMENT_REQUIRED, CACHE_DISABLED)),
    ERR_FORBIDDEN(new CustomErrorHandler(FORBIDDEN)),
    ERR_NOT_FOUND(new CustomErrorHandler(NOT_FOUND, logLevel:INFO)),
    ERR_METHOD_NOT_ALLOWED(new CustomErrorHandler(METHOD_NOT_ALLOWED)),
    ERR_NOT_ACCEPTABLE(new CustomErrorHandler(NOT_ACCEPTABLE)),
    ERR_PROXY_AUTHENTICATION_REQUIRED(new CustomErrorHandler(PROXY_AUTHENTICATION_REQUIRED, CACHE_DISABLED)),
    ERR_REQUEST_TIMEOUT(new RetryErrorHandler(REQUEST_TIMEOUT, 5s, CACHE_DISABLED)),
    ERR_CONFLICT(new CustomErrorHandler(CONFLICT)),
    ERR_GONE(new CustomErrorHandler(GONE)),
    ERR_LENGTH_REQUIRED(new CustomErrorHandler(LENGTH_REQUIRED)),
    ERR_PRECONDITION_FAILED(new CustomErrorHandler(PRECONDITION_FAILED)),
    ERR_REQUEST_TOO_LONG(new CustomErrorHandler(REQUEST_TOO_LONG)),
    ERR_REQUEST_URI_TOO_LONG(new CustomErrorHandler(REQUEST_URI_TOO_LONG)),
    ERR_UNSUPPORTED_MEDIA_TYPE(new CustomErrorHandler(UNSUPPORTED_MEDIA_TYPE)),
    ERR_REQUESTED_RANGE_NOT_SATISFIABLE(new CustomErrorHandler(REQUESTED_RANGE_NOT_SATISFIABLE)),
    ERR_EXPECTATION_FAILED(new CustomErrorHandler(EXPECTATION_FAILED)),
    ERR_INSUFFICIENT_SPACE_ON_RESOURCE(new CustomErrorHandler(INSUFFICIENT_SPACE_ON_RESOURCE)),
    ERR_METHOD_FAILURE(new RetryErrorHandler(METHOD_FAILURE, 2Second)),
    ERR_MISDIRECTED_REQUEST(new CustomErrorHandler(MISDIRECTED_REQUEST)),
    ERR_UNPROCESSABLE_ENTITY(new CustomErrorHandler(UNPROCESSABLE_ENTITY)),
    ERR_LOCKED(new RetryErrorHandler(LOCKED, 5Second, logLevel:INFO)),
    ERR_TOO_EARLY(new RetryErrorHandler(TOO_EARLY, 1Second)),
    ERR_UPGRADE_REQUIRED(new CustomErrorHandler(UPGRADE_REQUIRED)),
    ERR_PRECONDITION_REQUIRED(new CustomErrorHandler(PRECONDITION_REQUIRED)),
    ERR_TOO_MANY_REQUESTS(new RetryErrorHandler(TOO_MANY_REQUESTS, 5Second, CACHE_DISABLED, WARN)),
    ERR_REQUEST_HEADER_FIELDS_TOO_LARGE(new CustomErrorHandler(REQUEST_HEADER_FIELDS_TOO_LARGE)),
    ERR_UNAVAILABLE_FOR_LEGAL_REASONS(new CustomErrorHandler(UNAVAILABLE_FOR_LEGAL_REASONS)),

    // 5xx Server Error
    ERR_SERVER_ERROR(new RetryErrorHandler(SERVER_ERROR, 2Second)),
    ERR_NOT_IMPLEMENTED(new CustomErrorHandler(NOT_IMPLEMENTED)),
    ERR_BAD_GATEWAY(new RetryErrorHandler(BAD_GATEWAY, 2Second)),
    ERR_SERVICE_UNAVAILABLE(new RetryErrorHandler(SERVICE_UNAVAILABLE, 5Second)),
    ERR_GATEWAY_TIMEOUT(new RetryErrorHandler(GATEWAY_TIMEOUT, 2Second)),
    ERR_HTTP_VERSION_NOT_SUPPORTED(new CustomErrorHandler(HTTP_VERSION_NOT_SUPPORTED)),
    ERR_VARIANT_ALSO_NEGOTIATES(new CustomErrorHandler(VARIANT_ALSO_NEGOTIATES)),
    ERR_INSUFFICIENT_STORAGE(new CustomErrorHandler(INSUFFICIENT_STORAGE)),
    ERR_LOOP_DETECTED(new CustomErrorHandler(LOOP_DETECTED)),
    ERR_NOT_EXTENDED(new CustomErrorHandler(NOT_EXTENDED)),
    ERR_NETWORK_AUTHENTICATION_REQUIRED(new CustomErrorHandler(NETWORK_AUTHENTICATION_REQUIRED));

    @val Handler handler;
    @val HttpStatus code;
    @val CacheStrategy cacheStrategy;
    @val LogLevel logLevel;

    private static final Map<Integer, ErrorHandlerType> MAP =
        Arrays.stream(values()).collect(Collectors.toMap(eht -> eht.code.code, Function.identity()));

    ErrorHandlerType(Handler handler) {
        this.handler = handler;
        this.code = handler.code();
        this.cacheStrategy = handler.cacheStrategy();
        this.logLevel = handler.logLevel();
    }

    public static @Nullable ErrorHandlerType getForCode(int code) {
        return MAP.get(code);
    }

    public static @Nullable ErrorHandlerType getForCode(HttpStatus code) {
        return getForCode(code.code);
    }

    @NullMarked
    public sealed interface Handler permits RetryErrorHandler, CustomErrorHandler {
        HttpStatus code();

        CacheStrategy cacheStrategy();

        LogLevel logLevel();
    }

    @NullMarked
    public record RetryErrorHandler(HttpStatus code, Time duration, CacheStrategy cacheStrategy=CACHE_TEMPORARY,
        LogLevel logLevel=ERROR) implements Handler {}

    @NullMarked
    public record CustomErrorHandler(HttpStatus code, CacheStrategy cacheStrategy=CACHE_TEMPORARY,
        LogLevel logLevel=ERROR)
        implements Handler {}
}

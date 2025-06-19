package org.lodder.subtools.sublibrary.util.http;

import static manifold.science.measures.TimeUnit.*;
import static manifold.science.util.UnitConstants.*;
import static org.lodder.subtools.sublibrary.CacheStrategy.*;
import static org.lodder.subtools.sublibrary.LogLevel.*;
import static org.lodder.subtools.sublibrary.util.Sleep.*;
import static org.lodder.subtools.sublibrary.util.http.HttpStatus.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import manifold.ext.props.rt.api.val;
import manifold.ext.rt.api.Self;
import manifold.science.measures.Time;
import name.falgout.jeffrey.throwing.ThrowingSupplier;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.CacheStrategy;
import org.lodder.subtools.sublibrary.LogLevel;
import org.lodder.subtools.sublibrary.util.function.QuadFunction;
import retrofit2.Call;
import retrofit2.Response;

@NullMarked
public class RetrofitService {

    private RetrofitService() {
        //hide utility class constructor
    }

    @NullMarked
    public record ErrorHandler<X extends Exception>(
        // predicate to test if this handler is applicable
        BiPredicate<HttpStatus, String> predicate,
        // Time to sleep before a retry. Null means no retry
        @Nullable Time sleepTimeBeforeRetry,
        // Custom exception creator to use instead of the default one. If non is supplied, the default one is used.
        // Parameters: http status code, error body
        BiFunction<HttpStatus, String, X> exception) {

        public boolean isApplicable(HttpStatus status, String message) {
            return predicate.test(status, message);
        }
    }

    public static <T, X extends Exception> ExecuteCall<T, X> handleExecution(ThrowingSupplier<Call<T>, X> supplier,
        QuadFunction<HttpStatus, String, CacheStrategy, LogLevel, X> defaultExceptionCreator) {
        return new ExecuteCall<>(supplier, defaultExceptionCreator);
    }

    @NullMarked
    public static class ExecuteCall<T, X extends Exception> {

        private final ThrowingSupplier<Call<T>, X> supplier;
        private final QuadFunction<HttpStatus, String, CacheStrategy, LogLevel, X> defaultExceptionCreator;
        private final List<ErrorHandler<X>> errorHandlers = new ArrayList<>();

        public ExecuteCall(ThrowingSupplier<Call<T>, X> supplier,
            QuadFunction<HttpStatus, String, CacheStrategy, LogLevel, X> defaultExceptionCreator) {
            this.supplier = supplier;
            this.defaultExceptionCreator = defaultExceptionCreator;
        }

        public @Self ExecuteCall<T, X> addErrorHandler(HttpStatus code, CacheStrategy cacheStrategy=CACHE_TEMPORARY,
            LogLevel logLevel=ERROR, @Nullable Time sleepTimeBeforeRetry=null) {
            return addErrorHandler(new ErrorHandler<>((c, _) -> c == code, sleepTimeBeforeRetry,
                (httpStatus, error) -> defaultExceptionCreator.apply(httpStatus, error, cacheStrategy, logLevel)));
        }

//        public @Self ExecuteCall<T, X> addErrorHandler(ErrorHandlerType errorHandlerType,
//            @Nullable CacheStrategy cacheStrategy=null, @Nullable LogLevel logLevel=null,
//            @Nullable Time sleepTimeBeforeRetry=null) {
//            return addErrorHandler(errorHandlerType.code, cacheStrategy != null ? cacheStrategy :
//                    errorHandlerType.cacheStrategy, logLevel != null ? logLevel : errorHandlerType.logLevel,
//                sleepTimeBeforeRetry != null ? sleepTimeBeforeRetry : errorHandlerType.sleepTimeBeforeRetry);
//        }

        public @Self ExecuteCall<T, X> addErrorHandler(ErrorHandler<X> errorHandle) {
            errorHandlers.add(errorHandle);
            return this;
        }

        @NullMarked
        public enum ErrorHandlerType {
            ERR_BAD_REQUEST(BAD_REQUEST),
            ERR_UNAUTHORIZED(UNAUTHORIZED, CACHE_DISABLED),
            ERR_PAYMENT_REQUIRED(PAYMENT_REQUIRED, CACHE_DISABLED),
            ERR_FORBIDDEN(FORBIDDEN),
            ERR_NOT_FOUND(NOT_FOUND, logLevel:INFO),
            ERR_METHOD_NOT_ALLOWED(METHOD_NOT_ALLOWED),
            ERR_NOT_ACCEPTABLE(NOT_ACCEPTABLE),
            ERR_PROXY_AUTHENTICATION_REQUIRED(PROXY_AUTHENTICATION_REQUIRED, CACHE_DISABLED),
            ERR_REQUEST_TIMEOUT(REQUEST_TIMEOUT, CACHE_DISABLED, sleepTimeBeforeRetry:5s),
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
            ERR_METHOD_FAILURE(METHOD_FAILURE, sleepTimeBeforeRetry:2Second),
            ERR_MISDIRECTED_REQUEST(MISDIRECTED_REQUEST),
            ERR_UNPROCESSABLE_ENTITY(UNPROCESSABLE_ENTITY),
            ERR_LOCKED(LOCKED, logLevel:INFO, sleepTimeBeforeRetry:5Second),
            ERR_TOO_EARLY(TOO_EARLY, sleepTimeBeforeRetry:1Second),
            ERR_UPGRADE_REQUIRED(UPGRADE_REQUIRED),
            ERR_PRECONDITION_REQUIRED(PRECONDITION_REQUIRED),
            ERR_TOO_MANY_REQUESTS(TOO_MANY_REQUESTS, CACHE_DISABLED, WARN, 5Second),
            ERR_REQUEST_HEADER_FIELDS_TOO_LARGE(REQUEST_HEADER_FIELDS_TOO_LARGE),
            ERR_UNAVAILABLE_FOR_LEGAL_REASONS(UNAVAILABLE_FOR_LEGAL_REASONS),

            // 5xx Server Error
            ERR_SERVER_ERROR(SERVER_ERROR, sleepTimeBeforeRetry:2Second),
            ERR_NOT_IMPLEMENTED(NOT_IMPLEMENTED),
            ERR_BAD_GATEWAY(BAD_GATEWAY, sleepTimeBeforeRetry:2Second),
            ERR_SERVICE_UNAVAILABLE(SERVICE_UNAVAILABLE, sleepTimeBeforeRetry:5Second),
            ERR_GATEWAY_TIMEOUT(GATEWAY_TIMEOUT, sleepTimeBeforeRetry:2Second),
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

            ErrorHandlerType(HttpStatus code, CacheStrategy cacheStrategy=CACHE_TEMPORARY, LogLevel logLevel=ERROR,
                @Nullable Time sleepTimeBeforeRetry=null) {
                this.code = code;
                this.cacheStrategy = cacheStrategy;
                this.logLevel = logLevel;
                this.sleepTimeBeforeRetry = sleepTimeBeforeRetry;
            }
        }

        public T execute() throws X {
            ErrorHandlerType.values().forEach(errorHandlerType -> addErrorHandler(errorHandlerType.code,
                errorHandlerType.cacheStrategy, errorHandlerType.logLevel, errorHandlerType.sleepTimeBeforeRetry));
            return executePrivate(true);
        }

        private T executePrivate(boolean canRetry) throws X {
            Response<T> response;
            try {
                response = supplier.get().execute();
            } catch (IOException e) {
                throw defaultExceptionCreator.apply(BAD_GATEWAY, e.getMessage(), CACHE_TEMPORARY, ERROR);
            }
            if (response.isSuccessful()) {
                return Objects.requireNonNull(response.body());
            } else {
                String errorBody;
                try {
                    errorBody = Objects.requireNonNull(response.errorBody()).string();
                } catch (IOException e) {
                    errorBody = "";
                }
                HttpStatus code = fromStatusCode(response.code());
                if (canRetry) {
                    for (ErrorHandler<X> errorHandler : errorHandlers) {
                        if (errorHandler.isApplicable(code, errorBody)) {
                            if (errorHandler.sleepTimeBeforeRetry == null) {
                                throw errorHandler.exception.apply(code, errorBody);
                            }
                            sleep(errorHandler.sleepTimeBeforeRetry);
                            return executePrivate(false);
                        }
                    }
                }
                throw defaultExceptionCreator.apply(SERVER_ERROR, errorBody, CACHE_TEMPORARY, ERROR);
            }
        }
    }
}

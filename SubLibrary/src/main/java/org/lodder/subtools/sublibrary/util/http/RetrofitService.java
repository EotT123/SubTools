package org.lodder.subtools.sublibrary.util.http;

import static manifold.science.measures.TimeUnit.*;
import static org.lodder.subtools.sublibrary.util.Sleep.*;
import static org.lodder.subtools.sublibrary.util.http.HttpStatus.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import manifold.ext.rt.api.Self;
import manifold.science.measures.Time;
import name.falgout.jeffrey.throwing.ThrowingSupplier;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import retrofit2.Call;
import retrofit2.Response;

@NullMarked
public class RetrofitService {

    private RetrofitService() {
        //hide utility class constructor
    }

    public record ErrorHandler<X extends Exception>(
        // predicate to test if this handler is applicable
        BiPredicate<HttpStatus, String> predicate,
        // Time to sleep before a retry
        Time sleepTimeBeforeRetry=1Second,
        // should retries be done?
        boolean retry=true,
        // Custom exception creator to use instead of the default one. If non is supplied, the default one is used.
        // Parameters: http status code, error body
        @Nullable BiFunction<HttpStatus, String, X> exception=null) {

        public boolean isApplicable(HttpStatus status, String message) {
            return predicate.test(status, message);
        }
    }

    public static <T, X extends Exception> ExecuteCall<T, X> handleExecution(ThrowingSupplier<Call<T>, X> supplier,
        Function<HttpStatus, X> defaultExceptionCreator) {
        return new ExecuteCall<>(supplier, defaultExceptionCreator);
    }

    @NullMarked
    public static class ExecuteCall<T, X extends Exception> {

        private final ThrowingSupplier<Call<T>, X> supplier;
        private final Function<HttpStatus, X> defaultExceptionCreator;
        private int retries = 1;
        private final List<ErrorHandler<X>> errorHandlers = new ArrayList<>();

        public ExecuteCall(ThrowingSupplier<Call<T>, X> supplier, Function<HttpStatus, X> defaultExceptionCreator) {
            this.supplier = supplier;
            this.defaultExceptionCreator = defaultExceptionCreator;
        }

        private record HandleException<T, X extends Exception>(Predicate<X> predicate,
            Function<X, T> exceptionFunction) {
        }

        public @Self ExecuteCall<T, X> addErrorHandler(HttpStatus code, Time sleepTimeBeforeRetry=1Second,
            boolean retry=true) {
            return addErrorHandler(new ErrorHandler<>((c, _) -> c == code, sleepTimeBeforeRetry, retry));
        }

        public @Self ExecuteCall<T, X> addErrorHandler(ErrorHandler<X> errorHandle) {
            errorHandlers.add(errorHandle);
            return this;
        }

        public @Self ExecuteCall<T, X> retries(int retries) {
            if (retries <= 0) {
                throw new IllegalStateException("Retries should be greater than 0");
            }
            this.retries = retries;
            return this;
        }

        public T execute() throws X {
            // 4xx Client Error
            addErrorHandler(BAD_REQUEST, retry:false);
            addErrorHandler(UNAUTHORIZED, retry:false);
            addErrorHandler(PAYMENT_REQUIRED, retry:false);
            addErrorHandler(FORBIDDEN, retry:false);
            addErrorHandler(NOT_FOUND, retry:false);
            addErrorHandler(METHOD_NOT_ALLOWED, retry:false);
            addErrorHandler(NOT_ACCEPTABLE, retry:false);
            addErrorHandler(PROXY_AUTHENTICATION_REQUIRED, retry:false);
            addErrorHandler(REQUEST_TIMEOUT, sleepTimeBeforeRetry:2Second);
            addErrorHandler(CONFLICT, retry:false);
            addErrorHandler(GONE, retry:false);
            addErrorHandler(LENGTH_REQUIRED, retry:false);
            addErrorHandler(PRECONDITION_FAILED, retry:false);
            addErrorHandler(REQUEST_TOO_LONG, retry:false);
            addErrorHandler(REQUEST_URI_TOO_LONG, retry:false);
            addErrorHandler(UNSUPPORTED_MEDIA_TYPE, retry:false);
            addErrorHandler(REQUESTED_RANGE_NOT_SATISFIABLE, retry:false);
            addErrorHandler(EXPECTATION_FAILED, retry:false);
            addErrorHandler(INSUFFICIENT_SPACE_ON_RESOURCE, retry:false);
            addErrorHandler(METHOD_FAILURE, sleepTimeBeforeRetry:2Second);
            addErrorHandler(MISDIRECTED_REQUEST, retry:false);
            addErrorHandler(UNPROCESSABLE_ENTITY, retry:false);
            addErrorHandler(LOCKED, sleepTimeBeforeRetry:2Second);
            addErrorHandler(TOO_EARLY, sleepTimeBeforeRetry:1Second);
            addErrorHandler(UPGRADE_REQUIRED, retry:false);
            addErrorHandler(PRECONDITION_REQUIRED, retry:false);
            addErrorHandler(TOO_MANY_REQUESTS, sleepTimeBeforeRetry:5Second);
            addErrorHandler(REQUEST_HEADER_FIELDS_TOO_LARGE, retry:false);
            addErrorHandler(UNAVAILABLE_FOR_LEGAL_REASONS, retry:false);

            // 5xx Server Error
            addErrorHandler(SERVER_ERROR, sleepTimeBeforeRetry:2Second);
            addErrorHandler(NOT_IMPLEMENTED, retry:false);
            addErrorHandler(BAD_GATEWAY, sleepTimeBeforeRetry:2Second);
            addErrorHandler(SERVICE_UNAVAILABLE, sleepTimeBeforeRetry:5Second);
            addErrorHandler(GATEWAY_TIMEOUT, sleepTimeBeforeRetry:2Second);
            addErrorHandler(HTTP_VERSION_NOT_SUPPORTED, retry:false);
            addErrorHandler(VARIANT_ALSO_NEGOTIATES, retry:false);
            addErrorHandler(INSUFFICIENT_STORAGE, retry:false);
            addErrorHandler(LOOP_DETECTED, retry:false);
            addErrorHandler(NOT_EXTENDED, retry:false);
            addErrorHandler(NETWORK_AUTHENTICATION_REQUIRED, retry:false);

            return executePrivate();
        }

        private T executePrivate() throws X {
            Response<T> response;
            try {
                response = supplier.get().execute();
            } catch (IOException e) {
                throw defaultExceptionCreator.apply(BAD_GATEWAY);
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
                if (retries <= 0) {
                    throw defaultExceptionCreator.apply(code);
                }
                for (ErrorHandler<X> errorHandler : errorHandlers) {
                    if (errorHandler.isApplicable(code, errorBody)) {
                        if (!errorHandler.retry) {
                            throw createException(errorHandler, code, errorBody);
                        }
                        sleep(errorHandler.sleepTimeBeforeRetry);
                        retries--;
                        executePrivate();
                    }
                }
                // unhandled exception
                throw defaultExceptionCreator.apply(code);
            }
        }

        private X createException(ErrorHandler<X> errorHandler, HttpStatus httpStatus, String errorBody) {
            if (errorHandler.exception == null) {
                return defaultExceptionCreator.apply(httpStatus);
            } else {
                return errorHandler.exception.apply(httpStatus, errorBody);
            }
        }
    }


}

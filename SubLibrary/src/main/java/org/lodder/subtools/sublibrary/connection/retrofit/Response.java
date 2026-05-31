package org.lodder.subtools.sublibrary.connection.retrofit;

import static java.util.Objects.*;
import static org.lodder.subtools.sublibrary.util.Sleep.*;
import static org.lodder.subtools.sublibrary.util.webpage.http.HttpStatus.*;
import static util.Utils.*;

import java.io.IOException;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import manifold.science.measures.Time;
import okhttp3.ResponseBody;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.connection.ErrorHandlerType;
import org.lodder.subtools.sublibrary.util.Sleep;
import org.lodder.subtools.sublibrary.util.webpage.http.HttpStatus;
import retrofit2.Call;

@SuppressWarnings("unchecked")
@NullMarked
public sealed interface Response<T> permits SuccessfulResponse, ErrorResponse {

    static <T> Response<T> execute(Call<T> call, List<ErrorHandler> errorHandlers, boolean retry) {
        try {
            retrofit2.Response<T> response = call.execute();
            HttpStatus statusCode = fromStatusCode(response.code());
            if (response.body() != null) {
                return new SuccessfulResponse<>(requireNonNull(response.body()), statusCode, response.message(),
                    response.headers());
            } else {
                ResponseBody errorBody = requireNonNull(response.errorBody());
                if (!retry) {
                    return new ErrorResponse(statusCode, response.message());
                }
                String stringBodyTemp;
                try {
                    stringBodyTemp = errorBody.string();
                } catch (IOException e) {
                    stringBodyTemp = "";
                }
                String stringBody = stringBodyTemp;
                ErrorHandler applicableErrorHandler =
                    errorHandlers.stream().filter(eh -> eh.isApplicable(statusCode, stringBody)).findAny().orElse(null);

                Supplier<ErrorResponse> errorResponseSupplier = () -> new ErrorResponse(statusCode, response.message());

                return (Response<T>) ifNotNullOrElseGet(applicableErrorHandler,
                    errorHandler -> switch (errorHandler) {
                        case RetryErrorHandler handler -> {
                            ifNotNullDo(handler.sleepTimeBeforeRetry, Sleep::sleep);
                            yield execute(call, List.of(), false);
                        }
                        case CustomErrorHandler h -> h.errorResponseFunction().apply(statusCode, response.message());
                    }, () ->
                        ifNotNullOrElseGet(ErrorHandlerType.getForCode(statusCode),
                            errorHandlerType -> switch (errorHandlerType.handler) {
                                case ErrorHandlerType.RetryErrorHandler handler -> {
                                    sleep(handler.duration);
                                    yield execute(call, List.of(), false);
                                }
                                case ErrorHandlerType.CustomErrorHandler _ -> errorResponseSupplier.get();
                            }, errorResponseSupplier::get));
            }
        } catch (IOException e) {
            return new ErrorResponse(BAD_GATEWAY, e.getMessage());
        }
    }

    @NullMarked
    sealed interface ErrorHandler permits RetryErrorHandler, CustomErrorHandler {
        // predicate to test if this handler is applicable
        BiPredicate<HttpStatus, String> predicate();

        default boolean isApplicable(HttpStatus status, String message) {
            return predicate().test(status, message);
        }
    }

    @NullMarked
    record RetryErrorHandler(
        // predicate to test if this handler is applicable
        BiPredicate<HttpStatus, String> predicate,
        // Time to sleep before a retry. Null means no sleep
        @Nullable Time sleepTimeBeforeRetry=null,
        // Runnable to execute before a retry
        Runnable runnableBeforeRetry) implements ErrorHandler {
    }

    @NullMarked
    record CustomErrorHandler(
        // predicate to test if this handler is applicable
        BiPredicate<HttpStatus, String> predicate,
        // Function to create a custom errorResponse
        BiFunction<HttpStatus, String, ErrorResponse> errorResponseFunction) implements ErrorHandler {
    }
}

package connection.retrofit;

import static java.util.Objects.*;
import static org.lodder.subtools.sublibrary.util.Sleep.*;
import static org.lodder.subtools.sublibrary.util.webpage.http.HttpStatus.*;
import static util.Utils.*;

import java.io.IOException;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import connection.ErrorHandlerType;
import manifold.science.measures.Time;
import okhttp3.ResponseBody;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.util.webpage.http.HttpStatus;
import retrofit2.Call;

@NullMarked
public sealed interface Response<T> permits SuccessfulResponse, ErrorResponse {

    static <T> Response<T> execute(Call<T> call, List<ErrorHandler> errorHandlers, boolean retry) {
        try {
            retrofit2.Response<T> response = call.execute();
            if (response.body() != null) {
                return new SuccessfulResponse<>(requireNonNull(response.body()), response.code(), response.message(),
                    response.headers());
            } else {
                ResponseBody errorBody = requireNonNull(response.errorBody());
                HttpStatus statusCode = fromStatusCode(response.code());
                if (statusCode == null) {
                    return new ErrorResponse(SERVER_ERROR, "Unknown status code [${response.code()}]");
                }
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

                BiFunction<HttpStatus, String, ErrorResponse> errorResponseFtn = ErrorResponse::new;

                Supplier<ErrorResponse> errorResponseSupplier =
                    () -> errorResponseFtn.apply(statusCode, response.message());

                return (Response<T>) ifNotNullOrElseGet(applicableErrorHandler, errorHandler -> {
                    if (errorHandler.sleepTimeBeforeRetry == null) {
                        return ifNullThen(errorHandler.errorResponseFunction(), errorResponseFtn)
                            .apply(statusCode, response.message());
                    }
                    sleep(errorHandler.sleepTimeBeforeRetry);
                    return execute(call, List.of(), false);
                }, () ->
                    ifNotNullOrElseGet(ErrorHandlerType.getForCode(statusCode), errorHandler -> {
                        if (errorHandler.sleepTimeBeforeRetry == null) {
                            return errorResponseSupplier.get();
                        }
                        sleep(errorHandler.sleepTimeBeforeRetry);
                        return execute(call, List.of(), false);
                    }, errorResponseSupplier::get));
            }
        } catch (IOException e) {
            return new ErrorResponse(BAD_GATEWAY, e.getMessage());
        }
    }

    @NullMarked
    record ErrorHandler(
        // predicate to test if this handler is applicable
        BiPredicate<HttpStatus, String> predicate,
        // Time to sleep before a retry. Null means no retry
        @Nullable Time sleepTimeBeforeRetry=null,
        // Optional function to create a custom errorResponse
        @Nullable BiFunction<HttpStatus, String, ErrorResponse> errorResponseFunction=null) {

        public boolean isApplicable(HttpStatus status, String message) {
            return predicate.test(status, message);
        }
    }
}

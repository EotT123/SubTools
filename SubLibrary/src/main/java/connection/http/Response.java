package connection.http;

import static org.lodder.subtools.sublibrary.util.Sleep.*;
import static org.lodder.subtools.sublibrary.util.webpage.http.HttpStatus.*;
import static util.Utils.*;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import connection.ErrorHandlerType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.util.webpage.http.HttpStatus;

@NullMarked
public sealed interface Response permits SuccessfulResponse, ErrorResponse {

    static Response execute(HttpClient httpClient, HttpRequest request,
        HttpResponse.BodyHandler<String> responseBodyHandler, List<ErrorHandler> errorHandlers, boolean retry) {
        try {
            HttpResponse<String> response = httpClient.send(request, responseBodyHandler);
            if (response.statusCode() / 200 == 1) {
                return new SuccessfulResponse(response.body(), response.statusCode(), response.headers());
            } else {
                HttpStatus statusCode = fromStatusCode(response.statusCode());
                if (statusCode == null) {
                    return new ErrorResponse(SERVER_ERROR, "Unknown status code [${response.statusCode()}]");
                }

                BiFunction<HttpStatus, String, ErrorResponse> errorResponseFtn = ErrorResponse::new;
                Supplier<ErrorResponse> errorResponseSupplier =
                    () -> errorResponseFtn.apply(statusCode, response.body());

                if (!retry) {
                    return errorResponseSupplier.get();
                }

                ErrorHandler applicableErrorHandler =
                    errorHandlers.stream().filter(eh -> eh.isApplicable(statusCode, response.body())).findAny()
                        .orElse(null);


                return ifNotNullOrElseGet(applicableErrorHandler, errorHandler -> {
                    if (errorHandler.runnableBeforeRetry == null) {
                        return ifNullThen(errorHandler.errorResponseFunction(), errorResponseFtn)
                            .apply(statusCode, response.body());
                    }
                    errorHandler.runnableBeforeRetry.run();
                    return execute(httpClient, request, responseBodyHandler, List.of(), false);
                }, () -> ifNotNullOrElseGet(ErrorHandlerType.getForCode(statusCode), errorHandler -> {
                    if (errorHandler.sleepTimeBeforeRetry == null) {
                        return errorResponseSupplier.get();
                    }
                    sleep(errorHandler.sleepTimeBeforeRetry);
                    return execute(httpClient, request, responseBodyHandler, List.of(), false);
                }, errorResponseSupplier::get));
            }
        } catch (IOException | InterruptedException e) {
            return new ErrorResponse(BAD_GATEWAY, e.getMessage());
        }
    }

    @NullMarked
    record ErrorHandler(
        // predicate to test if this handler is applicable
        BiPredicate<HttpStatus, String> predicate,
        // Runnable to execute before a retry
        @Nullable Runnable runnableBeforeRetry=null,
        // Optional function to create a custom errorResponse
        @Nullable BiFunction<HttpStatus, String, ErrorResponse> errorResponseFunction=null) {

        public boolean isApplicable(HttpStatus status, String message) {
            return predicate.test(status, message);
        }
    }
}

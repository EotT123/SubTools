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
import manifold.science.measures.Time;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.util.Sleep;
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

                Supplier<ErrorResponse> errorResponseSupplier = () -> new ErrorResponse(statusCode, response.body());

                if (!retry) {
                    return errorResponseSupplier.get();
                }

                ErrorHandler applicableErrorHandler =
                    errorHandlers.stream().filter(eh -> eh.isApplicable(statusCode, response.body())).findAny()
                        .orElse(null);

                return ifNotNullOrElseGet(applicableErrorHandler,
                    errorHandler -> switch (errorHandler) {
                        case RetryErrorHandler handler -> {
                            handler.runnableBeforeRetry.run();
                            yield execute(httpClient, request, responseBodyHandler, List.of(), false);
                        }
                        case CustomErrorHandler customErrorHandler ->
                            customErrorHandler.errorResponseFunction().apply(statusCode, response.body());
                    }, () -> ifNotNullOrElseGet(ErrorHandlerType.getForCode(statusCode),
                        errorHandlerType -> switch (errorHandlerType.handler) {
                            case ErrorHandlerType.RetryErrorHandler handler -> {
                                sleep(handler.duration);
                                yield execute(httpClient, request, responseBodyHandler, List.of(), false);
                            }
                            case ErrorHandlerType.CustomErrorHandler _ -> errorResponseSupplier.get();
                        }, errorResponseSupplier::get));
            }
        } catch (IOException | InterruptedException e) {
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
        // Runnable to execute before a retry
        Runnable runnableBeforeRetry) implements ErrorHandler {

        static RetryErrorHandler sleep(BiPredicate<HttpStatus, String> predicate, Time duration) {
            return new RetryErrorHandler(predicate, () -> Sleep.sleep(duration));
        }
    }

    @NullMarked
    record CustomErrorHandler(
        // predicate to test if this handler is applicable
        BiPredicate<HttpStatus, String> predicate,
        // Function to create a custom errorResponse
        BiFunction<HttpStatus, String, ErrorResponse> errorResponseFunction) implements ErrorHandler {
    }
}

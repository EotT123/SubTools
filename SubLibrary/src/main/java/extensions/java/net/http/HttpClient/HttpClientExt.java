package extensions.java.net.http.HttpClient;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.util.Arrays;

import connection.http.Response;
import connection.http.Response.ErrorHandler;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import org.jspecify.annotations.NullMarked;

@Extension
@NullMarked
public class HttpClientExt {

    private HttpClientExt() {
        // Hide Utility Class Constructor
    }

    public static Response call(@This HttpClient httpClient, HttpRequest request, ErrorHandler... errorHandlers) {
        return call(httpClient, request, HttpResponse.BodyHandlers.ofString(), errorHandlers);
    }

    public static Response call(@This HttpClient httpClient, HttpRequest request,
        BodyHandler<String> responseBodyHandler, ErrorHandler... errorHandlers) {
        return Response.execute(httpClient, request, responseBodyHandler, Arrays.asList(errorHandlers), true);
    }
}

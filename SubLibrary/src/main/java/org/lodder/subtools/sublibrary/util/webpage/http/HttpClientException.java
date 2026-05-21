package org.lodder.subtools.sublibrary.util.webpage.http;

import java.io.IOException;
import java.io.Serial;
import java.net.HttpURLConnection;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class HttpClientException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;
    @val int responseCode;
    @val String responseMessage;

    public HttpClientException(HttpURLConnection connection) {
        this(null, connection);
    }

    public HttpClientException(@Nullable Throwable cause, @Nullable HttpURLConnection connection) {
        super(cause);
        this.responseCode = getResponseCode(connection);
        this.responseMessage = getResponseMessage(connection);
    }

    private int getResponseCode(@Nullable HttpURLConnection connection) {
        if (connection != null) {
            try {
                return connection.getResponseCode();
            } catch (IOException e) {
                // continue
            }
        }
        return -1;
    }

    private String getResponseMessage(@Nullable HttpURLConnection connection) {
        if (connection != null) {
            try {
                return connection.getResponseMessage();
            } catch (IOException e) {
                // continue
            }
        }
        return "";
    }

    @Override
    public String toString() {
        return "HttpClientException $responseCode $responseMessage";
    }
}

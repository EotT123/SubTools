package org.lodder.subtools.sublibrary.connection.retrofit;

import manifold.ext.props.rt.api.val;
import okhttp3.Headers;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.util.webpage.http.HttpStatus;

@NullMarked
public final class SuccessfulResponse<T> implements Response<T> {

    @val T body;
    @val HttpStatus code;
    @val String message;
    @val Headers headers;

    SuccessfulResponse(T body, HttpStatus code, String message, Headers headers) {
        this.body = body;
        this.code = code;
        this.message = message;
        this.headers = headers;
    }
}

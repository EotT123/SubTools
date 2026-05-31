package org.lodder.subtools.sublibrary.connection.http;

import java.net.http.HttpHeaders;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.util.webpage.http.HttpStatus;

@NullMarked
public final class SuccessfulResponse implements Response {

    @val String body;
    @val HttpStatus code;
    @val HttpHeaders headers;

    SuccessfulResponse(String body, HttpStatus code, HttpHeaders headers) {
        this.body = body;
        this.code = code;
        this.headers = headers;
    }
}

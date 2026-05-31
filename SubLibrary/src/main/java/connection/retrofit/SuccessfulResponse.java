package connection.retrofit;

import manifold.ext.props.rt.api.val;
import okhttp3.Headers;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class SuccessfulResponse<T> implements Response<T> {

    @val T body;
    @val int code;
    @val String message;
    @val Headers headers;

    SuccessfulResponse(T body, int code, String message, Headers headers) {
        this.body = body;
        this.code = code;
        this.message = message;
        this.headers = headers;
    }
}

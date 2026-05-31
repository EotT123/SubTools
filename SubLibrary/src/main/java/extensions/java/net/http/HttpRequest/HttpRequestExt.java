package extensions.java.net.http.HttpRequest;

import java.net.http.HttpRequest;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@Extension
@NullMarked
public class HttpRequestExt {

    private HttpRequestExt() {
        // Hide Utility Class Constructor
    }

    @NullMarked
    public static class Builder {


        private Builder() {
            // Hide Utility Class Constructor
        }

        public static HttpRequest.Builder addHeaderIfNotNull(@This HttpRequest.Builder builder,
            String name, @Nullable String value) {
            if (value != null) {
                builder.header(name, value);
            }
            return builder;
        }
    }
}


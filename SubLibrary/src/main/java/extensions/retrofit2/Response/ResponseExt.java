package extensions.retrofit2.Response;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import retrofit2.Response;

@Extension
public class ResponseExt {

    private ResponseExt() {
        // Hide Utility Class Constructor
    }

    public static <T> T data(@This Response<T> response) {
        return response.body().data;
    }
}

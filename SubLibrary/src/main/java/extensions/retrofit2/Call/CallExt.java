package extensions.retrofit2.Call;

import java.util.List;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import org.jspecify.annotations.NullMarked;
import retrofit.Response;
import retrofit.Response.Response2;
import retrofit2.Call;

@Extension
@NullMarked
public class CallExt {

    private CallExt() {
        // Hide Utility Class Constructor
    }

    public static <T> Response<T> call(@This Call<T> call) {
        return Response.execute(call, List.of(), true);
    }

    public static <T> Response2<T> addErrorHandler(@This Call<T> call, Response.ErrorHandler errorHandler) {
        return new Response2<>(call).addErrorHandlers(errorHandler);
    }
}
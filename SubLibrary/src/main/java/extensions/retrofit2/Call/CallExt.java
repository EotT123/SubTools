package extensions.retrofit2.Call;

import java.util.Arrays;

import connection.retrofit.Response;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import org.jspecify.annotations.NullMarked;
import retrofit2.Call;

@Extension
@NullMarked
public class CallExt {

    private CallExt() {
        // Hide Utility Class Constructor
    }

    public static <T> Response<T> call(@This Call<T> call, Response.ErrorHandler... errorHandlers) {
        return Response.execute(call, Arrays.asList(errorHandlers), true);
    }
}
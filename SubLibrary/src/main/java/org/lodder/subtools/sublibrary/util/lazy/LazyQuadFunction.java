package org.lodder.subtools.sublibrary.util.lazy;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.util.Nothing;
import org.lodder.subtools.sublibrary.util.throwingfunction.ThrowingQuadFunction;

@NullMarked
public class LazyQuadFunction<T extends @Nullable Object, U extends @Nullable Object, V extends @Nullable Object,
    W extends @Nullable Object, R extends @Nullable Object>
    extends LazyThrowingQuadFunction<T, U, V, W, R, Nothing> {

    public LazyQuadFunction(ThrowingQuadFunction<T, U, V, W, R, Nothing> function) {
        super(function);
    }

    public R apply(T arg1, U arg2, V arg3, W arg4) {
        return super.apply(arg1, arg2, arg3, arg4);
    }
}

package org.lodder.subtools.sublibrary.util.lazy;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.util.throwingfunction.ThrowingQuadFunction;

@NullMarked
public class LazyThrowingQuadFunction<T extends @Nullable Object, U extends @Nullable Object,
    V extends @Nullable Object, W extends @Nullable Object, R extends @Nullable Object, X extends Exception> {

    private final Object lock = new Object();

    private final ThrowingQuadFunction<T, U, V, W, R, X> function;

    private R object;

    private volatile boolean initialized = false;

    public LazyThrowingQuadFunction(ThrowingQuadFunction<T, U, V, W, R, X> function) {
        this.function = function;
    }

    public R apply(T arg1, U arg2, V arg3, W arg4) throws X {
        if (!initialized) {
            synchronized (lock) {
                if (!initialized) {
                    object = function.apply(arg1, arg2, arg3, arg4);
                    initialized = true;
                }
            }
        }
        return object;
    }

    public void reset() {
        initialized = false;
    }
}

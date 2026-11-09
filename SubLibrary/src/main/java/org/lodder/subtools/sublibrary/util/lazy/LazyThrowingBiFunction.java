package org.lodder.subtools.sublibrary.util.lazy;

import name.falgout.jeffrey.throwing.ThrowingBiFunction;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class LazyThrowingBiFunction<T extends @Nullable Object, U extends @Nullable Object,
    R extends @Nullable Object, X extends Exception> {

    private final ThrowingBiFunction<T, U, R, X> function;
    private R object;
    private volatile boolean initialized = false;

    public LazyThrowingBiFunction(ThrowingBiFunction<T, U, R, X> function) {
        this.function = function;
    }

    public R apply(T arg1, U arg2) throws X {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    object = function.apply(arg1, arg2);
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

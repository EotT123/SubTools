package org.lodder.subtools.sublibrary.util.lazy;

import name.falgout.jeffrey.throwing.ThrowingFunction;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class LazyThrowingFunction<T extends @Nullable Object, S extends @Nullable Object, X extends Exception> {

    private final ThrowingFunction<T, S, X> function;

    private S object;

    private final Object lock = new Object();

    private volatile boolean initialized = false;

    public LazyThrowingFunction(ThrowingFunction<T, S, X> function) {
        this.function = function;
    }

    public S apply(T arg) throws X {
        if (!initialized) {
            synchronized (lock) {
                if (!initialized) {
                    object = function.apply(arg);
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

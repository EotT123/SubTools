package org.lodder.subtools.sublibrary.util.lazy;

import name.falgout.jeffrey.throwing.ThrowingSupplier;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class LazyThrowingSupplier<T extends @Nullable Object, X extends Exception> {

    private final ThrowingSupplier<T, X> supplier;
    private @Nullable T object;
    private volatile boolean initialized = false;

    public LazyThrowingSupplier(ThrowingSupplier<T, X> supplier) {
        this.supplier = supplier;
    }

    public T get() throws X {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    object = supplier.get();
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

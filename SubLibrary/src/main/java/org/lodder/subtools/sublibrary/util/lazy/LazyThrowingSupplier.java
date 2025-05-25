package org.lodder.subtools.sublibrary.util.lazy;

import name.falgout.jeffrey.throwing.ThrowingConsumer;
import name.falgout.jeffrey.throwing.ThrowingSupplier;

public class LazyThrowingSupplier<T, X extends Exception> {

    private final ThrowingSupplier<T, X> supplier;

    private T object;

    private final Object lock = new Object();

    private volatile boolean initialized = false;

    public LazyThrowingSupplier(ThrowingSupplier<T, X> supplier) {
        this.supplier = supplier;
    }

    public LazyThrowingSupplier(T value) {
        supplier = null;
        object = value;
        initialized = true;
    }

    public T get() throws X {
        if (!initialized) {
            synchronized (lock) {
                if (!initialized) {
                    object = supplier.get();
                    initialized = true;
                }
            }
        }
        return object;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void doIfInitialized(ThrowingConsumer<T, X> consumer) throws X {
        if (initialized) {
            consumer.accept(object);
        }
    }

    public void reset() {
        initialized = false;
    }
}

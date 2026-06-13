package org.lodder.subtools.sublibrary.util.lazy;

import static manifold.ext.props.rt.api.PropOption.*;

import manifold.ext.props.rt.api.get;
import manifold.ext.props.rt.api.val;
import manifold.ext.props.rt.api.var;
import name.falgout.jeffrey.throwing.ThrowingConsumer;
import name.falgout.jeffrey.throwing.ThrowingSupplier;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class LazyObjectNullableThrow<T extends @Nullable Object, X extends Exception> {

    @val ThrowingSupplier<T, X> supplier;
    private T object;
    @get @var(Private) volatile boolean initialized = false;

    public LazyObjectNullableThrow(ThrowingSupplier<T, X> supplier) {
        this.supplier = supplier;
    }

    public LazyObjectNullableThrow(T value) {
        this(() -> value);
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

    public void doIfInitialized(ThrowingConsumer<T, X> consumer) throws X {
        if (initialized) {
            consumer.accept(object);
        }
    }

    public void reset() {
        initialized = false;
    }
}

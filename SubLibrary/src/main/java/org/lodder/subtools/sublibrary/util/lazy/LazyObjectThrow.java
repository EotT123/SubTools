package org.lodder.subtools.sublibrary.util.lazy;

import static java.util.Objects.*;

import name.falgout.jeffrey.throwing.ThrowingSupplier;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class LazyObjectThrow<T, X extends Exception> extends LazyObjectNullableThrow<T, X> {

    public LazyObjectThrow(ThrowingSupplier<T, X> supplier) {
        super(supplier);
    }

    public LazyObjectThrow(T value) {
        super(value);
    }

    @Override
    public T get() throws X {
        return requireNonNull(super.get());
    }
}

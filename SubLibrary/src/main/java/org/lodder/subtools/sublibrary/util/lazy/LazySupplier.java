package org.lodder.subtools.sublibrary.util.lazy;

import name.falgout.jeffrey.throwing.ThrowingSupplier;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.util.Nothing;

@NullMarked
public class LazySupplier<T> extends LazyThrowingSupplier<T, Nothing> {

    public LazySupplier(ThrowingSupplier<T, Nothing> supplier) {
        super(supplier);
    }

    public LazySupplier(T value) {
        super(value);
    }
}

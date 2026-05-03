package org.lodder.subtools.sublibrary.util.lazy;

import java.util.function.Supplier;

import name.falgout.jeffrey.throwing.Nothing;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class LazyObjectNullable<T extends @Nullable Object> extends LazyObjectNullableThrow<T, Nothing> {

    public LazyObjectNullable(Supplier<T> supplier) {
        super(supplier::get);
    }

    public LazyObjectNullable(T value) {
        super(value);
    }
}

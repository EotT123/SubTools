package org.lodder.subtools.sublibrary.util.lazy;

import name.falgout.jeffrey.throwing.ThrowingBiFunction;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.util.Nothing;

@NullMarked
public class LazyBiFunction<T extends @Nullable Object, S extends @Nullable Object, V extends @Nullable Object>
    extends LazyThrowingBiFunction<T, S, V, Nothing> {

    public LazyBiFunction(ThrowingBiFunction<T, S, V, Nothing> function) {
        super(function);
    }

    public V apply(T arg1, S arg2) {
        return super.apply(arg1, arg2);
    }
}

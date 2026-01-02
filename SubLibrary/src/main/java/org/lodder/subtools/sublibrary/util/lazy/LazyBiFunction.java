package org.lodder.subtools.sublibrary.util.lazy;

import java.util.function.BiFunction;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.util.Nothing;

@NullMarked
public class LazyBiFunction<T extends @Nullable Object, S extends @Nullable Object, V extends @Nullable Object>
    extends LazyThrowingBiFunction<T, S, V, Nothing> {

    public LazyBiFunction(BiFunction<T, S, V> function) {
        super(function::apply);
    }

    @Override
    public V apply(T arg1, S arg2) {
        return super.apply(arg1, arg2);
    }
}

package org.lodder.subtools.sublibrary.util.lazy;

import name.falgout.jeffrey.throwing.ThrowingFunction;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.util.Nothing;

@NullMarked
public class LazyFunction<T extends @Nullable Object, S extends @Nullable Object>
    extends LazyThrowingFunction<T, S, Nothing> {

    public LazyFunction(ThrowingFunction<T, S, Nothing> function) {
        super(function);
    }

    public S apply(T arg) {
        return super.apply(arg);
    }
}

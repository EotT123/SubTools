package org.lodder.subtools.sublibrary.util.lazy;

import name.falgout.jeffrey.throwing.ThrowingFunction;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.util.Nothing;

@NullMarked
public class LazyFunction<T, S> extends LazyThrowingFunction<T, S, Nothing> {

    public LazyFunction(ThrowingFunction<T, S, Nothing> function) {
        super(function);
    }
}

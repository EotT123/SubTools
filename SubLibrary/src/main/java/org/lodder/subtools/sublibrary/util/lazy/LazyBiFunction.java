package org.lodder.subtools.sublibrary.util.lazy;

import name.falgout.jeffrey.throwing.ThrowingBiFunction;
import org.lodder.subtools.sublibrary.util.Nothing;

public class LazyBiFunction<T, S, V> extends LazyThrowingBiFunction<T, S, V, Nothing> {

    public LazyBiFunction(ThrowingBiFunction<T, S, V, Nothing> function) {
        super(function);
    }

}

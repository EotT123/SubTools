package org.lodder.subtools.sublibrary.util.lazy;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.util.Nothing;
import org.lodder.subtools.sublibrary.util.throwingfunction.ThrowingQuadFunction;

@NullMarked
public class LazyQuadFunction<T, U, V, W, R> extends LazyThrowingQuadFunction<T, U, V, W, R, Nothing> {

    public LazyQuadFunction(ThrowingQuadFunction<T, U, V, W, R, Nothing> function) {
        super(function);
    }
}

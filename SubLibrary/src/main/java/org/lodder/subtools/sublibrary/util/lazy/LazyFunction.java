package org.lodder.subtools.sublibrary.util.lazy;

import java.util.function.Function;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.util.Nothing;

@NullMarked
public class LazyFunction<T extends @Nullable Object, S extends @Nullable Object>
    extends LazyThrowingFunction<T, S, Nothing> {

    public LazyFunction(Function<T, S> function) {
        super(function::apply);
    }

    @Override
    public S apply(T arg) {
        return super.apply(arg);
    }
}

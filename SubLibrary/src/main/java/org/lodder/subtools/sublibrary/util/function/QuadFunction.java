package org.lodder.subtools.sublibrary.util.function;

import java.util.function.Function;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
@NullMarked
public interface QuadFunction<T extends @Nullable Object, U extends @Nullable Object, V extends @Nullable Object,
    W extends @Nullable Object, R extends @Nullable Object> {

    R apply(T t, U u, V v, W w);

    default <S> QuadFunction<T, U, V, W, S> andThen(Function<? super R, S> after) {
        return (t, u, v, w) -> after.apply(apply(t, u, v, w));
    }

}

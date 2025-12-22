package org.lodder.subtools.sublibrary.util.function;

import static java.util.Objects.*;

import org.jspecify.annotations.NullMarked;

@FunctionalInterface
@NullMarked
public interface TriConsumer<T, U, V> {
    void accept(T t, U u, V v);

    default TriConsumer<T, U, V> andThen(TriConsumer<? super T, ? super U, ? super V> after) {
        requireNonNull(after);
        return (a, b, c) -> {
            accept(a, b, c);
            after.accept(a, b, c);
        };
    }
}
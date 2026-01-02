package org.lodder.subtools.sublibrary.util.function;

import static java.util.Objects.*;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
@NullMarked
public interface TriConsumer<T extends @Nullable Object, U extends @Nullable Object, V extends @Nullable Object> {
    void accept(T t, U u, V v);

    default TriConsumer<T, U, V> andThen(TriConsumer<? super T, ? super U, ? super V> after) {
        requireNonNull(after);
        return (a, b, c) -> {
            accept(a, b, c);
            after.accept(a, b, c);
        };
    }
}
package org.lodder.subtools.sublibrary.util.throwingfunction;

import java.util.Optional;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.util.function.QuadFunction;

@FunctionalInterface
@NullMarked
public interface ThrowingQuadFunction<T extends @Nullable Object, U extends @Nullable Object,
    V extends @Nullable Object, W extends @Nullable Object, R extends @Nullable Object, E extends Exception> {

    R apply(T var1, U var2, V var3, W var4) throws E;

    static <T extends @Nullable Object, U extends @Nullable Object, V extends @Nullable Object,
        W extends @Nullable Object, R extends @Nullable Object> QuadFunction<T, U, V, W, R> sneaky(
        ThrowingQuadFunction<? super T, ? super U, ? super V, ? super W, ? extends R, ?> function) {
        return (t1, t2, t3, t4) -> {
            try {
                return function.apply(t1, t2, t3, t4);
            } catch (Exception ex) {
                return SneakyThrowUtil.sneakyThrow(ex);
            }
        };
    }

    static <T extends @Nullable Object, U extends @Nullable Object, V extends @Nullable Object,
        W extends @Nullable Object, R extends @Nullable Object> QuadFunction<T, U, V, W, Optional<R>> lifted(
        ThrowingQuadFunction<T, U, V, W, R, ?> f) {
        return ((ThrowingQuadFunction) f).lift();
    }

    default QuadFunction<T, U, V, W, Optional<R>> lift() {
        return (arg1, arg2, arg3, arg4) -> {
            try {
                return Optional.ofNullable(this.apply(arg1, arg2, arg3, arg4));
            } catch (Exception var4) {
                return Optional.empty();
            }
        };
    }
}

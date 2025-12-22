package org.lodder.subtools.sublibrary.util.throwingfunction;

import java.util.Optional;

import org.apache.commons.lang3.function.TriFunction;
import org.jspecify.annotations.NullMarked;

@FunctionalInterface
@NullMarked
public interface ThrowingTriFunction<T, U, V, R, E extends Exception> {
    R apply(T var1, U var2, V var3) throws E;

    static <T, U, V, R> TriFunction<T, U, V, R> sneaky(
        ThrowingTriFunction<? super T, ? super U, ? super V, ? extends R, ?> function) {
        return (t1, t2, t3) -> {
            try {
                return function.apply(t1, t2, t3);
            } catch (Exception ex) {
                return SneakyThrowUtil.sneakyThrow(ex);
            }
        };
    }

    static <T, U, V, R> TriFunction<T, U, V, Optional<R>> lifted(ThrowingTriFunction<T, U, V, R, ?> f) {
        return ((ThrowingTriFunction) f).lift();
    }

    default TriFunction<T, U, V, Optional<R>> lift() {
        return (arg1, arg2, arg3) -> {
            try {
                return Optional.ofNullable(this.apply(arg1, arg2, arg3));
            } catch (Exception var4) {
                return Optional.empty();
            }
        };
    }
}

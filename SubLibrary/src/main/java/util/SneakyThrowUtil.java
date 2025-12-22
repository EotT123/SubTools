package util;

import java.util.function.Consumer;
import java.util.function.Function;

import name.falgout.jeffrey.throwing.ThrowingConsumer;
import name.falgout.jeffrey.throwing.ThrowingFunction;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class SneakyThrowUtil {
    private SneakyThrowUtil() {
    }

    public static <T extends Exception, R> R sneakyThrow(Exception t) throws T {
        throw (T) t;
    }

    public static <T, R, E extends Exception> Function<T, R> sneaky(
        ThrowingFunction<? super T, ? extends R, E> function) throws E {
        return t -> {
            try {
                return function.apply(t);
            } catch (final Exception ex) {
                return sneakyThrow(ex);
            }
        };
    }

    public static <T, E extends Exception> Consumer<T> sneaky(ThrowingConsumer<? super T, E> function) throws E {
        return t -> {
            try {
                function.accept(t);
            } catch (final Exception ex) {
                sneakyThrow(ex);
            }
        };
    }
}
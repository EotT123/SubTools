package util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import name.falgout.jeffrey.throwing.ThrowingConsumer;
import name.falgout.jeffrey.throwing.ThrowingFunction;
import name.falgout.jeffrey.throwing.ThrowingPredicate;
import name.falgout.jeffrey.throwing.ThrowingToDoubleFunction;
import name.falgout.jeffrey.throwing.ThrowingToIntFunction;
import name.falgout.jeffrey.throwing.ThrowingToLongFunction;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class SneakyThrowUtil {
    private SneakyThrowUtil() {
    }

    public static <T extends Exception, R extends @Nullable Object> R sneakyThrow(Exception t) throws T {
        throw (T) t;
    }

    public static <T extends @Nullable Object, R extends @Nullable Object, E extends Exception> Function<T, R> sneaky(
        ThrowingFunction<? super T, ? extends R, E> function) throws E {
        return t -> {
            try {
                return function.apply(t);
            } catch (Exception ex) {
                return sneakyThrow(ex);
            }
        };
    }

    public static <T extends @Nullable Object, E extends Exception> ToIntFunction<T> sneaky(
        ThrowingToIntFunction<? super T, E> function) throws E {
        return t -> {
            try {
                return function.applyAsInt(t);
            } catch (Exception ex) {
                return sneakyThrow(ex);
            }
        };
    }

    public static <T extends @Nullable Object, E extends Exception> ToLongFunction<T> sneaky(
        ThrowingToLongFunction<? super T, E> function) throws E {
        return t -> {
            try {
                return function.applyAsLong(t);
            } catch (Exception ex) {
                return sneakyThrow(ex);
            }
        };
    }

    public static <T extends @Nullable Object, E extends Exception> ToDoubleFunction<T> sneaky(
        ThrowingToDoubleFunction<? super T, E> function) throws E {
        return t -> {
            try {
                return function.applyAsDouble(t);
            } catch (Exception ex) {
                return sneakyThrow(ex);
            }
        };
    }

    public static <T extends @Nullable Object, E extends Exception> Consumer<T> sneaky(
        ThrowingConsumer<? super T, E> function) throws E {
        return t -> {
            try {
                function.accept(t);
            } catch (Exception ex) {
                sneakyThrow(ex);
            }
        };
    }

    public static <T extends @Nullable Object, E extends Exception> Predicate<T> sneaky(
        ThrowingPredicate<? super T, E> function) throws E {
        return t -> {
            try {
                return function.test(t);
            } catch (Exception ex) {
                return sneakyThrow(ex);
            }
        };
    }
}
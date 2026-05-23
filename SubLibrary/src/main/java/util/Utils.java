package util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collector;

import name.falgout.jeffrey.throwing.ThrowingConsumer;
import name.falgout.jeffrey.throwing.ThrowingFunction;
import name.falgout.jeffrey.throwing.ThrowingRunnable;
import name.falgout.jeffrey.throwing.ThrowingSupplier;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class Utils {

    private Utils() {
        // hide utility class constructor
    }

    public static <T, K, V> Collector<T, Map<K, V>, Map<K, V>> mapCollector(BiConsumer<Map<K, V>, T> accumulator) {
        return mapCollector(HashMap::new, accumulator);
    }

    public static <T, R extends Map<K, V>, K, V> Collector<T, R, R> mapCollector(Supplier<R> supplier,
        BiConsumer<R, T> accumulator) {
        return Collector.of(
            supplier,
            accumulator,
            (R m1, R m2) -> {
                m1.putAll(m2);
                return m1;
            });
    }

    public static <T, K> Collector<T, Set<K>, Set<K>> setCollector(BiConsumer<Set<K>, T> accumulator) {
        return setCollector(HashSet::new, accumulator);
    }

    public static <T, R extends Set<K>, K> Collector<T, R, R> setCollector(Supplier<R> supplier,
        BiConsumer<R, T> accumulator) {
        return Collector.of(
            supplier,
            accumulator,
            (R m1, R m2) -> {
                m1.addAll(m2);
                return m1;
            });
    }

    public static <T, X extends Exception> void ifNotNullDo(@Nullable T value, ThrowingConsumer<T, X> consumer)
        throws X {
        if (value != null) {
            consumer.accept(value);
        }
    }

    public static <T, X extends Exception> void ifNotNullOrElseDo(@Nullable T value, ThrowingConsumer<T, X> consumer,
        ThrowingRunnable<X> runnable)
        throws X {
        if (value != null) {
            consumer.accept(value);
        } else {
            runnable.run();
        }
    }

    public static <T, R extends @Nullable Object, X extends Exception> @Nullable R ifNotNull(@Nullable T value,
        ThrowingFunction<T, R, X> mapper) throws X {
        return value != null ? mapper.apply(value) : null;
    }

    public static <T, R extends @Nullable Object, X extends Exception> R ifNotNullOrElse(@Nullable T value,
        ThrowingFunction<T, R, X> mapper, R orElseValue) throws X {
        return value != null ? mapper.apply(value) : orElseValue;
    }

    public static <T, R extends @Nullable Object, X extends Exception> R ifNotNullOrElseGet(@Nullable T value,
        ThrowingFunction<T, R, X> mapper, Supplier<R> orElseSupplier) throws X {
        return value != null ? mapper.apply(value) : orElseSupplier.get();
    }

    @Contract("!null,_ -> param1; null,_ -> param2")
    public static <S extends @Nullable Object, T extends S> S ifNullThen(@Nullable T value, S orElseValue) {
        return value != null ? value : orElseValue;
    }

    public static <S extends @Nullable Object, T extends S, X extends Exception> S ifNullThenGet(
        @Nullable T value, ThrowingSupplier<S, X> orElseSupplier) throws X {
        return value != null ? value : orElseSupplier.get();
    }

    @Contract("!null,_ -> param1; null,_ -> fail")
    public static <T, X extends Exception> T ifNullThrow(@Nullable T value, Supplier<X> exceptionSupplier) throws X {
        if (value != null) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }
}

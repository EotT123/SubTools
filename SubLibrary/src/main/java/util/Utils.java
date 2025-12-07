package util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class Utils {

    private Utils() {
        // hide utility class constructor
    }

    public static <T, K, V> Collector<T, Map<K, V>, Map<K, V>> mapCollector(
        BiConsumer<Map<K, V>, T> accumulator) {
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

    public static <T, R> @Nullable R ifNotNull(@Nullable T value, Function<T, R> mapper) {
        return value != null ? mapper.apply(value) : null;
    }

    public static <T, R> R ifNotNullOrElse(@Nullable T value, Function<T, R> mapper, R orElseValue) {
        return value != null ? mapper.apply(value) : orElseValue;
    }

    public static <T, R> R ifNotNullOrElseGet(@Nullable T value, Function<T, R> mapper, Supplier<R> orElseSupplier) {
        return value != null ? mapper.apply(value) : orElseSupplier.get();
    }

    public static <T> T ifNullThen(@Nullable T value, T orElseValue) {
        return value != null ? value : orElseValue;
    }

    public static <T> T ifNullThenGet(@Nullable T value, Supplier<T> orElseSupplier) {
        return value != null ? value : orElseSupplier.get();
    }
}

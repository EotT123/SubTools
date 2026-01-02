package extensions.java.util.stream.Stream;

import static java.util.function.Predicate.*;
import static util.SneakyThrowUtil.*;
import static util.Utils.*;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.DoubleStream;
import java.util.stream.Gatherer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;
import name.falgout.jeffrey.throwing.ThrowingConsumer;
import name.falgout.jeffrey.throwing.ThrowingFunction;
import name.falgout.jeffrey.throwing.ThrowingPredicate;
import name.falgout.jeffrey.throwing.ThrowingToDoubleFunction;
import name.falgout.jeffrey.throwing.ThrowingToIntFunction;
import name.falgout.jeffrey.throwing.ThrowingToLongFunction;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("unused")
@Extension
@NullMarked
public class StreamExt {

    private StreamExt() {
        // Hide Utility Class Constructor
    }

    // MAP //

    public static <T extends @Nullable Object, R> Stream<R> mapFilterNonNull(@This Stream<T> stream,
        Function<? super T, ? extends @Nullable R> mapper) {
        return stream.gather(Gatherer.ofSequential((_, element, downstream) -> {
            ifNotNullDo(mapper.apply(element), downstream::push);
            return true;
        }));
    }

    public static <T extends @Nullable Object, R, X extends Exception> Stream<R> mapFilterNonNullEx(
        @This Stream<T> stream, ThrowingFunction<? super T, ? extends @Nullable R, X> mapper) throws X {
        return stream.gather(Gatherer.ofSequential((_, element, downstream) -> {
            try {
                ifNotNullDo(mapper.apply(element), downstream::push);
            } catch (Exception e) {
                sneakyThrow(e);
            }
            return true;
        }));
    }

    public static <T extends @Nullable Object, R extends @Nullable Object, X extends Exception> Stream<R> mapEx(
        @This Stream<T> stream, ThrowingFunction<? super T, ? extends R, X> mapper) throws X {
        return stream.map(sneaky(mapper));
    }

    public static <T extends @Nullable Object, R extends @Nullable Object, X extends Exception> Stream<R> mapIgnoreEx(
        @This Stream<T> stream, ThrowingFunction<? super T, ? extends R, X> mapper) {
        try {
            return stream.mapEx(mapper);
        } catch (Exception e) {
            return Stream.empty();
        }
    }

    // FLATMAP //

    public static <T extends @Nullable Object, R extends Stream<V>, V extends @Nullable Object, X extends Exception> Stream<V> flatMapEx(
        @This Stream<T> stream, ThrowingFunction<? super T, ? extends R, X> mapper) throws X {
        return stream.flatMap(sneaky(mapper));
    }

    public static <T extends @Nullable Object, R extends Stream<V>, V extends @Nullable Object, X extends Exception> Stream<V> flatMapIgnoreEx(
        @This Stream<T> stream, ThrowingFunction<? super T, ? extends R, X> mapper) {
        try {
            return stream.flatMapEx(mapper);
        } catch (Exception e) {
            return Stream.empty();
        }
    }

    public static <T extends @Nullable Object, R extends Optional<V>, V extends @Nullable Object,
        X extends Exception> Stream<V> flatMapOptionalEx(
        @This Stream<T> stream, ThrowingFunction<? super T, ? extends R, X> mapper) throws X {
        return stream.flatMap(sneaky(mapper).andThen(Optional::stream));
    }

    public static <T extends @Nullable Object, R extends Optional<V>, V extends @Nullable Object,
        X extends Exception> Stream<V> flatMapOptionalIgnoreEx(
        @This Stream<T> stream, ThrowingFunction<? super T, ? extends R, X> mapper) {
        try {
            return stream.flatMapOptionalEx(mapper);
        } catch (Exception e) {
            return Stream.empty();
        }
    }

    // MAP TO INT

    public static <T extends @Nullable Object, X extends Exception> IntStream mapToIntEx(@This Stream<T> stream,
        ThrowingToIntFunction<? super T, X> mapper) throws X {
        return stream.mapToInt(sneaky(mapper));
    }

    public static <T extends @Nullable Object, X extends Exception> IntStream mapToIntIgnoreEx(@This Stream<T> stream,
        ThrowingToIntFunction<? super T, X> mapper) throws X {
        try {
            return stream.mapToIntEx(mapper);
        } catch (Exception e) {
            return IntStream.empty();
        }
    }


    // MAP TO LONG

    public static <T extends @Nullable Object, X extends Exception> LongStream mapToLongEx(@This Stream<T> stream,
        ThrowingToLongFunction<? super T, X> mapper) throws X {
        return stream.mapToLong(sneaky(mapper));
    }

    public static <T extends @Nullable Object, X extends Exception> LongStream mapToLongIgnoreEx(@This Stream<T> stream,
        ThrowingToLongFunction<? super T, X> mapper) throws X {
        try {
            return stream.mapToLongEx(mapper);
        } catch (Exception e) {
            return LongStream.empty();
        }
    }

    // MAP TO DOUBLE

    public static <T extends @Nullable Object, X extends Exception> DoubleStream mapToDoubleEx(@This Stream<T> stream,
        ThrowingToDoubleFunction<? super T, X> mapper) throws X {
        return stream.mapToDouble(sneaky(mapper));
    }

    public static <T extends @Nullable Object, X extends Exception> DoubleStream mapToDoubleIgnoreEx(
        @This Stream<T> stream, ThrowingToDoubleFunction<? super T, X> mapper) throws X {
        try {
            return stream.mapToDoubleEx(mapper);
        } catch (Exception e) {
            return DoubleStream.empty();
        }
    }

    // FOR EACH //

    public static <T extends @Nullable Object, X extends Exception> void forEachEx(@This Stream<T> stream,
        ThrowingConsumer<T, X> consumer) throws X {
        stream.forEach(sneaky(consumer));
    }


    public static <T extends @Nullable Object, R> Stream<R> filterCast(@This Stream<T> stream, Class<R> type) {
        return stream.filter(type::isInstance).map(type::cast);
    }

    public static <T extends @Nullable Object> Stream<T> forEachContinue(@This Stream<T> stream, Consumer<T> consumer) {
        return stream.map(e -> {
            consumer.accept(e);
            return e;
        });
    }

    public static <T extends @Nullable Object, X extends Exception> Stream<T> forEachContinueEx(@This Stream<T> stream,
        ThrowingConsumer<T, X> consumer) throws X {
        return forEachContinue(stream, sneaky(consumer));
    }

    // FILTER //


    public static <T extends @Nullable Object, X extends Exception> Stream<T> filterEx(@This Stream<T> stream,
        ThrowingPredicate<T, X> predicate) throws X {
        return stream.filter(sneaky(predicate));
    }


    public static <T extends @Nullable Object> Stream<T> filterGet(@This Stream<Optional<T>> stream) {
        return stream.filter(Optional::isPresent).map(Optional::get);
    }

    public static <T extends @Nullable Object> @Self Stream<T> exclude(@This Stream<T> stream, Predicate<T> filter) {
        return stream.filter(not(filter));
    }

    @SuppressWarnings("SimplifyStreamApiCallChains")
    public static <T extends @Nullable Object> @Self Stream<T> use(@This Stream<T> stream, Consumer<T> consumer) {
        return stream.map(e -> {
            consumer.accept(e);
            return e;
        });
    }
}
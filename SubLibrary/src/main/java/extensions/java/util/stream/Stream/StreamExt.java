package extensions.java.util.stream.Stream;

import static util.SneakyThrowUtil.*;

import java.util.Optional;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import lombok.experimental.UtilityClass;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import name.falgout.jeffrey.throwing.ThrowingConsumer;
import name.falgout.jeffrey.throwing.ThrowingFunction;
import name.falgout.jeffrey.throwing.stream.ThrowingStream;

@UtilityClass
@Extension
public class StreamExt {

    public static <T, E extends Exception> ThrowingStream<T, E> asThrowingStream(@This Stream<T> stream,
        Class<E> exceptionType) {
        return ThrowingStream.of(stream, exceptionType);
    }

    public static <T> T[] toTypedArray(@This Stream<T> stream, IntFunction<T[]> generator) {
        return stream.toArray(generator);
    }

    public static <T, R, X extends Exception> Stream<R> mapEx(@This Stream<T> stream,
        ThrowingFunction<? super T, ? extends R, X> mapper) throws X {
        return stream.map(sneaky(mapper));
    }

    public static <T, R, X extends Exception> Stream<R> mapIgnoreEx(@This Stream<T> stream,
        ThrowingFunction<? super T, ? extends R, X> mapper) {
        try {
            return stream.mapEx(mapper);
        } catch (Exception e) {
            return Stream.empty();
        }
    }

    public static <T, R extends Iterable<V>, V, X extends Exception> Stream<V> flatMapEx(@This Stream<T> stream,
        ThrowingFunction<? super T, ? extends R, X> mapper) throws X {
        return stream.flatMap(sneaky(mapper.andThen(Iterable::stream)));
    }

    public static <T, R extends Iterable<V>, V, X extends Exception> Stream<V> flatMapIgnoreEx(@This Stream<T> stream,
        ThrowingFunction<? super T, ? extends R, X> mapper) {
        try {
            return stream.flatMapEx(mapper);
        } catch (Exception e) {
            return Stream.empty();
        }
    }

    public static <T, R extends Optional<V>, V, X extends Exception> Stream<V> flatMapOptionalEx(@This Stream<T> stream,
        ThrowingFunction<? super T, ? extends R, X> mapper) throws X {
        return stream.flatMap(sneaky(mapper).andThen(Optional::stream));
    }

    public static <T, R extends Optional<V>, V, X extends Exception> Stream<V> flatMapOptionalIgnoreEx(
        @This Stream<T> stream, ThrowingFunction<? super T, ? extends R, X> mapper) {
        try {
            return stream.flatMapOptionalEx(mapper);
        } catch (Exception e) {
            return Stream.empty();
        }
    }

    public static <T, X extends Exception> void forEachEx(@This Stream<T> stream,
        ThrowingConsumer<T, X> consumer) throws X {
        stream.forEach(sneaky(consumer));
    }


    public static <T, R> Stream<R> filterCast(@This Stream<T> stream, Class<R> type) {
        return stream.filter(type::isInstance).map(type::cast);
    }
}

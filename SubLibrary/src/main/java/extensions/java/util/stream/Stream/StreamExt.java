package extensions.java.util.stream.Stream;

import java.util.Set;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.experimental.UtilityClass;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
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

    public static <T> Set<T> toSet(@This Stream<T> stream) {
        return stream.collect(Collectors.toSet());
    }

//    public static <T, R, X extends Exception> Stream<R> flatMapThrowing(
//        @This Stream<T> stream, ThrowingFunction<T, Stream<R>, X> mapper) {
//        Function<T, Stream<R>> function = t -> {
//            try {
//                return mapper.apply(t);
//            } catch (Exception ex) {
//                return sneakyThrow(ex);
//            }
//        };
//        return stream.flatMap(function);
//    }
}

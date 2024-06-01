package extensions.java.util.stream.Stream;

import java.util.stream.Stream;

import lombok.experimental.UtilityClass;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import name.falgout.jeffrey.throwing.stream.ThrowingStream;

@UtilityClass
@Extension
public class StreamExt {

    public static <T, E extends Exception> ThrowingStream<T, E> asThrowingStream(@This Stream<T> stream, Class<E> exceptionType) {
        return ThrowingStream.of(stream, exceptionType);
    }
}

package extensions.java.lang.Iterable;

import static util.SneakyThrowUtil.*;

import java.util.Collection;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.experimental.UtilityClass;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import name.falgout.jeffrey.throwing.ThrowingConsumer;

@UtilityClass
@Extension
public class IterableExt {

    public static <T> Stream<T> stream(@This Iterable<T> iterable) {
        return  StreamSupport.stream(iterable.spliterator(), false);
    }

    public static int size(@This Iterable<?> iterable) {
        return iterable instanceof Collection<?> collection ? collection.size() : (int) iterable.stream().count();
    }

    public static <T, X extends Exception> void forEachEx(@This Iterable<T> iterable,
        ThrowingConsumer<T, X> consumer) throws X {
        iterable.forEach(sneaky(consumer));
    }
}
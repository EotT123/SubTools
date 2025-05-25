package extensions.java.util.Spliterator;

import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;

@Extension
public class SpliteratorExt {

    private SpliteratorExt() {
        // hide utility class constructor
    }

    public static <T> Stream<T> stream(@This Spliterator<T> spliterator) {
        return StreamSupport.stream(spliterator, false);
    }
}
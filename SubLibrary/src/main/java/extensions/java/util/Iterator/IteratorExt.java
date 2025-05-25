package extensions.java.util.Iterator;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;

@Extension
public class IteratorExt {

    private IteratorExt() {
        // hide utility class constructor
    }

    public static <E> Stream<E> stream(@This Iterator<E> iterator) {
        return Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED).stream();
    }
}
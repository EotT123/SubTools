package org.lodder.subtools.sublibrary.util.lazy;

import java.util.function.Supplier;

import name.falgout.jeffrey.throwing.Nothing;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class LazyObject<T> extends LazyObjectThrow<T, Nothing> {

    public LazyObject(Supplier<T> supplier) {
        super(supplier::get);
    }

    public LazyObject(T value) {
        super(value);
    }
}

package org.lodder.subtools.sublibrary.util.lazy;

import name.falgout.jeffrey.throwing.ThrowingRunnable;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.util.Nothing;

@NullMarked
public class LazyRunnable extends LazyThrowingRunnable<Nothing> {

    public LazyRunnable(ThrowingRunnable<Nothing> runnable) {
        super(runnable);
    }
}

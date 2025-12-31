package org.lodder.subtools.sublibrary.util.lazy;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.util.Nothing;

@NullMarked
public class LazyRunnable extends LazyThrowingRunnable<Nothing> {

    public LazyRunnable(Runnable runnable) {
        super(runnable::run);
    }
}

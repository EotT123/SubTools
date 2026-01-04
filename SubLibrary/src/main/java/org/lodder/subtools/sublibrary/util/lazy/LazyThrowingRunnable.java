package org.lodder.subtools.sublibrary.util.lazy;

import name.falgout.jeffrey.throwing.ThrowingRunnable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class LazyThrowingRunnable<X extends Exception> {

    private final ThrowingRunnable<X> runnable;
    private volatile boolean initialized = false;

    public LazyThrowingRunnable(ThrowingRunnable<X> runnable) {
        this.runnable = runnable;
    }

    public void run() throws X {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    runnable.run();
                    initialized = true;
                }
            }
        }
    }

    public void reset() {
        initialized = false;
    }
}

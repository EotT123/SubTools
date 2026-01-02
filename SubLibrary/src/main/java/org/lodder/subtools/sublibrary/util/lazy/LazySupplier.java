package org.lodder.subtools.sublibrary.util.lazy;

import java.util.function.Supplier;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.util.Nothing;

@NullMarked
public class LazySupplier<T extends @Nullable Object> extends LazyThrowingSupplier<T, Nothing> {

    public LazySupplier(Supplier<T> supplier) {
        super(supplier::get);
    }

    @Override
    public T get() {
        return super.get();
    }
}

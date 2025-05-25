package org.lodder.subtools.sublibrary.cache;

import java.io.Serial;
import java.io.Serializable;

import org.jspecify.annotations.NullMarked;

@NullMarked
final class ExpiringSerializableCacheObject<T extends Serializable> extends ExpiringCacheObject<T> {

    @Serial
    private static final long serialVersionUID = 1L;

    ExpiringSerializableCacheObject(T value) {
        super(value);
    }
}

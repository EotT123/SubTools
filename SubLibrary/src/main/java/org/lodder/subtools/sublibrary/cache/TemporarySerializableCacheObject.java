package org.lodder.subtools.sublibrary.cache;

import java.io.Serial;
import java.io.Serializable;

import manifold.science.measures.Time;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class TemporarySerializableCacheObject<T extends Serializable> extends TemporaryCacheObject<T> {

    @Serial
    private static final long serialVersionUID = 1L;

    TemporarySerializableCacheObject(Time timeToLive, T value) {
        super(timeToLive, value);
    }
}

package org.lodder.subtools.sublibrary.cache;

import java.io.Serializable;
import java.util.function.Function;

import manifold.ext.props.rt.api.val;
import manifold.science.measures.Time;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public sealed interface CacheObject<V extends @Nullable Object> extends Serializable
    permits ExpiringCacheObject, TemporaryCacheObject {

    @val Time created;
    @val V value;
    @val Time age;

    void updateLastAccessed();

    boolean isExpired(Time ttl);

    String toString(Function<V, String> valueToStringMapper);
}

package org.lodder.subtools.sublibrary.cache;

import java.io.Serializable;
import java.util.function.Function;

import manifold.ext.props.rt.api.val;
import manifold.science.measures.Time;
import org.jspecify.annotations.NullMarked;

@NullMarked
public sealed interface CacheObject<V> extends Serializable permits ExpiringCacheObject, TemporaryCacheObject {

    @val Time created;
    @val V value;
    @val Time age;

    void updateLastAccessed();

    boolean isExpired(Time ttl);

    String toString(Function<V, String> valueToStringMapper);

    static <V> CacheObject<V> fromString(String string, Function<String, V> valueToObjectMapper) {
        return ExpiringCacheObject.fromString(string, valueToObjectMapper)
            .orElseGet(() -> TemporaryCacheObject.fromString(string, valueToObjectMapper)
                .orElseThrow(() -> new IllegalStateException("Could not parse value: $string")));
    }
}

package org.lodder.subtools.sublibrary.cache;

import java.util.function.Function;

import manifold.ext.props.rt.api.val;

public sealed interface CacheObject<T> permits ExpiringCacheObject, TemporaryCacheObject {

    @val long created;
    @val T value;
    @val long age;

    void updateLastAccessed();

    boolean isExpired(long ttl);

    String toString(Function<T, String> valueToStringMapper);

    static <T> CacheObject<T> fromString(String string, Function<String, T> valueToObjectMapper) {
        return ExpiringCacheObject.fromString(string, valueToObjectMapper)
            .orElseGet(() -> TemporaryCacheObject.fromString(string, valueToObjectMapper)
                .orElseThrow(() -> new IllegalStateException("Could not parse value: $string")));
    }
}

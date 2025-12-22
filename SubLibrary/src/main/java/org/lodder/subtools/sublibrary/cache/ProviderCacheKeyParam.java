package org.lodder.subtools.sublibrary.cache;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record ProviderCacheKeyParam(String key, @Nullable String value) implements
    Comparable<ProviderCacheKeyParam>, Serializable {

    public ProviderCacheKeyParam(String key, @Nullable Object value) {
        this(key, value == null ? null : String.valueOf(value));
    }

    @Override
    public String toString() {
        return key + ":" + value;
    }

    @Override
    public int compareTo(ProviderCacheKeyParam o) {
        return Comparator.comparing(ProviderCacheKeyParam::key).compare(this, o);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof ProviderCacheKeyParam that
            && Objects.equals(key, that.key) && Objects.equals(value, that.value));
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}

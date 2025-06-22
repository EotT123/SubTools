package org.lodder.subtools.sublibrary.cache;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
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
    public int compareTo(@NotNull ProviderCacheKeyParam o) {
        return Comparator.comparing(ProviderCacheKeyParam::key).compare(this, o);
    }

    @Override public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProviderCacheKeyParam that = (ProviderCacheKeyParam) o;
        return Objects.equals(key, that.key) && Objects.equals(value, that.value);
    }

    @Override public int hashCode() {
        return Objects.hash(key, value);
    }

    //    public static ProviderCacheKeyParam parse(String value){
//        String[] split = value.split(DELIMITER);
//        return new ProviderCacheKeyParam(split[0], split[1]);
//    }
}

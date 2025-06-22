package org.lodder.subtools.sublibrary.cache;

import java.io.Serializable;
import java.util.Comparator;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record ProviderCacheKeyParam(String key, @Nullable Object value) implements
    Comparable<ProviderCacheKeyParam>, Serializable {

    private static final String DELIMITER = "~/:/~";

    @Override
    public String toString() {
        return key + DELIMITER + value;
    }

    @Override
    public int compareTo(@NotNull ProviderCacheKeyParam o) {
        return Comparator.comparing(ProviderCacheKeyParam::key).compare(this, o);
    }


//    public static ProviderCacheKeyParam parse(String value){
//        String[] split = value.split(DELIMITER);
//        return new ProviderCacheKeyParam(split[0], split[1]);
//    }
}

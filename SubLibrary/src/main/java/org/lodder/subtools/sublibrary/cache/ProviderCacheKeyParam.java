package org.lodder.subtools.sublibrary.cache;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record ProviderCacheKeyParam(String key, @Nullable String value) {

    private static final String DELIMITER = "/:/";

    @Override
    public String toString() {
        return key + DELIMITER + value;
    }

//    public static ProviderCacheKeyParam parse(String value){
//        String[] split = value.split(DELIMITER);
//        return new ProviderCacheKeyParam(split[0], split[1]);
//    }
}

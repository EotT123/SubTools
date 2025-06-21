package org.lodder.subtools.sublibrary.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class ProviderCacheKey extends ProviderCacheKeyCommon {

    public ProviderCacheKey(String provider, String type, List<ProviderCacheKeyParam> providerCacheKeyParams=
        new ArrayList<ProviderCacheKeyParam>()) {
        super(provider, type, providerCacheKeyParams);
    }

//    public static ProviderCacheKey parse(String value){
//
//    }

    public Stream<ProviderCacheKeySub> getSubKeyStream() {
        return providerCacheKeyParams.stream()
            .map(cacheKeyParam -> new ProviderCacheKeySub(provider, type, cacheKeyParam));
    }
}

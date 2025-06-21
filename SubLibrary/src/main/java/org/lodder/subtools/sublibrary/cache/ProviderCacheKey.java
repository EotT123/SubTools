package org.lodder.subtools.sublibrary.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public final class ProviderCacheKey extends ProviderCacheKeyCommon {

    private final List<ProviderCacheKeyParam> providerCacheKeyIdParams;
    private final List<ProviderCacheKeyParam> providerCacheKeyOtherParams;

    public ProviderCacheKey(String provider, String type, List<ProviderCacheKeyParam> providerCacheKeyKeyParams=
        new ArrayList<ProviderCacheKeyParam>(), List<ProviderCacheKeyParam> providerCacheKeyParams=
        new ArrayList<ProviderCacheKeyParam>()) {
        super(provider, type,
            Stream.of(providerCacheKeyKeyParams, providerCacheKeyParams).flatMap(List::stream).toList());
        this.providerCacheKeyIdParams = providerCacheKeyKeyParams;
        this.providerCacheKeyOtherParams = providerCacheKeyParams;
    }

//    public static ProviderCacheKey parse(String value){
//
//    }

    public Stream<ProviderCacheKeySub> getSubKeyStream() {
        return providerCacheKeyIdParams.stream().map(cacheKeyParam ->
            new ProviderCacheKeySub(provider, type,
                Stream.of(Stream.of(cacheKeyParam), providerCacheKeyOtherParams.stream()).flatMap(Function.identity())
                    .toList()));
    }
}

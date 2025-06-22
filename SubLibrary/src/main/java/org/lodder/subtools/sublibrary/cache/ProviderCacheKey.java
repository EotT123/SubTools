package org.lodder.subtools.sublibrary.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public final class ProviderCacheKey extends ProviderCacheKeyCommon {

    private final List<ProviderCacheKeyParam> idParams;
    private final List<ProviderCacheKeyParam> otherParams;

    public ProviderCacheKey(String provider, String type, List<ProviderCacheKeyParam> providerCacheKeyKeyParams=
        new ArrayList<ProviderCacheKeyParam>(), List<ProviderCacheKeyParam> providerCacheKeyParams=
        new ArrayList<ProviderCacheKeyParam>()) {
        super(provider, type,
            Stream.of(providerCacheKeyKeyParams, providerCacheKeyParams).flatMap(List::stream).toList());
        this.idParams = providerCacheKeyKeyParams;
        this.otherParams = providerCacheKeyParams;
    }

//    public static ProviderCacheKey parse(String value){
//
//    }

    public Stream<ProviderCacheKeySub> getSubKeyStream() {
        return idParams.stream().map(cacheKeyParam ->
            new ProviderCacheKeySub(provider, type,
                Stream.of(Stream.of(cacheKeyParam), otherParams.stream()).flatMap(Function.identity())
                    .toList()));
    }

    @Override public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ProviderCacheKey that = (ProviderCacheKey) o;
        return Objects.equals(idParams, that.idParams) && Objects.equals(otherParams, that.otherParams);
    }

    @Override public int hashCode() {
        return Objects.hash(super.hashCode(), idParams, otherParams);
    }
}

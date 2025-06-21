package org.lodder.subtools.sublibrary.cache;

import java.util.List;
import java.util.stream.Collectors;

import manifold.ext.props.rt.api.val;

public abstract sealed class ProviderCacheKeyCommon permits ProviderCacheKeySub, ProviderCacheKey {

    private static final String DELIMITER = "/-/";
    @val String provider;
    @val String type;
    @val List<ProviderCacheKeyParam> providerCacheKeyParams;

    public ProviderCacheKeyCommon(String provider, String type, List<ProviderCacheKeyParam> providerCacheKeyParams) {
        this.provider = provider;
        this.type = type;
        this.providerCacheKeyParams = providerCacheKeyParams;
    }

//    public static ProviderCacheKeyCommon parse(String value){
//
//    }

    @Override
    public String toString() {
        return provider + DELIMITER + type + DELIMITER +
            providerCacheKeyParams.stream().map(ProviderCacheKeyParam::toString).collect(Collectors.joining(DELIMITER));
    }
}

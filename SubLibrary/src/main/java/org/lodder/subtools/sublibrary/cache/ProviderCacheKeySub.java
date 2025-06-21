package org.lodder.subtools.sublibrary.cache;

import java.util.List;

public final class ProviderCacheKeySub extends ProviderCacheKeyCommon {

    public ProviderCacheKeySub(String provider, String type, ProviderCacheKeyParam providerCacheKeyParam) {
        super(provider, type, List.of(providerCacheKeyParam));
    }
}

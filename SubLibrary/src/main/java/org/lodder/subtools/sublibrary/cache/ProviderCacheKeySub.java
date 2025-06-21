package org.lodder.subtools.sublibrary.cache;

import java.util.List;

public final class ProviderCacheKeySub extends ProviderCacheKeyCommon {

    public ProviderCacheKeySub( String provider, String type,  List<ProviderCacheKeyParam> providerCacheKeyParams) {
       super(provider, type, providerCacheKeyParams);
    }
}

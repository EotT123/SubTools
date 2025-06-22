package org.lodder.subtools.sublibrary.cache;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import manifold.ext.props.rt.api.val;

public abstract sealed class ProviderCacheKeyCommon implements Serializable permits ProviderCacheKeySub,
    ProviderCacheKey {

    private static final String DELIMITER = "~/-/~";
    @val String provider;
    @val String type;
    @val List<ProviderCacheKeyParam> params;

    public ProviderCacheKeyCommon(String provider, String type, List<ProviderCacheKeyParam> params) {
        this.provider = provider;
        this.type = type;
        this.params = params.stream().sorted().toList();
    }

//    public static ProviderCacheKeyCommon parse(String value){
//
//    }

    @Override public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProviderCacheKeyCommon that = (ProviderCacheKeyCommon) o;
        return Objects.equals(provider, that.provider) &&
            Objects.equals(type, that.type) &&
            Objects.equals(params, that.params);
    }

    @Override public int hashCode() {
        return Objects.hash(provider, type, params);
    }

    @Override
    public String toString() {
        return provider + DELIMITER + type + DELIMITER +
            params.stream().map(ProviderCacheKeyParam::toString).collect(Collectors.joining(DELIMITER));
    }
}

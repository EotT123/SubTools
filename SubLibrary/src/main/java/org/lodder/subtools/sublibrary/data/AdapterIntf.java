package org.lodder.subtools.sublibrary.data;

import java.util.function.UnaryOperator;

import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.Manager.CacheKey;
import org.lodder.subtools.sublibrary.Manager.CacheKeyBuilder;
import org.lodder.subtools.sublibrary.cache.CacheType;

public interface AdapterIntf {
    @val String provider;
    @val Manager manager;

    default CacheKey getCache(String operation) {
        return getCache(operation, b->b);
    }

    default CacheKey getCache(String operation, UnaryOperator<CacheKeyBuilder> CacheKeyBuilderFunction) {
        return manager.getCache(CacheType.DISK,
            CacheKeyBuilderFunction.apply(new CacheKeyBuilder(provider, operation)));
    }
}

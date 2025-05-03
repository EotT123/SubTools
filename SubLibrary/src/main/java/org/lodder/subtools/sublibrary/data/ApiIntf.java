package org.lodder.subtools.sublibrary.data;

import java.util.function.UnaryOperator;

import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.Manager.CacheKey;
import org.lodder.subtools.sublibrary.Manager.CacheKeyBuilder;
import org.lodder.subtools.sublibrary.cache.CacheType;

public interface ApiIntf {

    @val Manager manager;
    @val String provider;

    default CacheKey getCache(String operation, UnaryOperator<CacheKeyBuilder> CacheKeyBuilderFunction) {
        return manager.getCache(CacheType.MEMORY,
            CacheKeyBuilderFunction.apply(new CacheKeyBuilder(provider, operation)));
    }
}

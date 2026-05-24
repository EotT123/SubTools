package org.lodder.subtools.sublibrary.data;

import java.util.function.UnaryOperator;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.Manager.CacheKey;
import org.lodder.subtools.sublibrary.Manager.CacheKeyBuilder;
import org.lodder.subtools.sublibrary.cache.CacheType;

@NullMarked
public interface ApiIntf {

    @val String provider;

    default CacheKey getCache(String operation, UnaryOperator<CacheKeyBuilder> CacheKeyBuilderFunction) {
        return Manager.getCache(CacheType.MEMORY,
            CacheKeyBuilderFunction.apply(new CacheKeyBuilder(provider, operation)));
    }
}

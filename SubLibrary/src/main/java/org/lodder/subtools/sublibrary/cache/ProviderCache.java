package org.lodder.subtools.sublibrary.cache;

import static manifold.ext.props.rt.api.PropOption.*;
import static util.Utils.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import manifold.ext.props.rt.api.val;
import manifold.science.measures.Time;
import name.falgout.jeffrey.throwing.ThrowingSupplier;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public abstract sealed class ProviderCache<V extends @Nullable Object> permits ProviderCacheMemory, ProviderCacheDisk {

    @val(Protected) final Map<ProviderCacheKey, CacheObject<V>> cacheMap;
    @val(Protected) final Map<ProviderCacheKeySub, ProviderCacheKey> keyMapperCache = new HashMap<>();
    @val(Protected) final Multimap<ProviderCacheKey, ProviderCacheKeySub> keyMapperCacheInverse =
        MultimapBuilder.hashKeys().arrayListValues().build();
    @val(Protected) final Set<ProviderCacheKeySub> invalidKeys = new HashSet<>();

    protected ProviderCache(@Nullable Integer maxItems=null) {
        this.cacheMap = maxItems != null ? new LRUMap<>(maxItems) : new HashMap<>();
    }

    public void put(ProviderCacheKey key, V value, @Nullable Time timeToLive=null) {
        if (timeToLive == null) {
            put(key, new ExpiringCacheObject<>(value));
        } else {
            put(key, new TemporaryCacheObject<>(timeToLive, value));
        }
    }

    protected void put(ProviderCacheKey key, CacheObject<V> value) {
        synchronized (cacheMap) {
            cacheMap.put(key, value);
            key.getSubKeyStream().forEach(subkey -> {
                ProviderCacheKey prevValue = keyMapperCache.put(subkey, key);
                keyMapperCacheInverse.put(key, subkey);
                if (prevValue != null) {
                    invalidKeys.add(subkey);
                }
            });
        }
    }

    public boolean contains(ProviderCacheKeyCommon key) {
        synchronized (cacheMap) {
            return switch (key) {
                case ProviderCacheKey k -> cacheMap.containsKey(k);
                case ProviderCacheKeySub k -> !invalidKeys.contains(k) &&
                    ifNotNullOrElse(keyMapperCache.get(k), cacheMap::containsKey, false);
            };
        }
    }

    public @Nullable V get(ProviderCacheKeyCommon key) {
        synchronized (cacheMap) {
            CacheObject<V> cacheObject = switch (key) {
                case ProviderCacheKey k -> cacheMap.get(k);
                case ProviderCacheKeySub k ->
                    invalidKeys.contains(k) ? null : ifNotNull(keyMapperCache.get(k), cacheMap::get);
            };
            return ifNotNull(cacheObject, obj -> {
                    obj.updateLastAccessed();
                    return obj.value;
                });
        }
    }

//    public List<Entry<ProviderCacheKey, CacheObject<V>>> get(
//        @Nullable BiPredicate<ProviderCacheKey, CacheObject<V>> keyFilter) {
//        synchronized (cacheMap) {
//            return cacheMap.entrySet().stream()
//                .filter(entry -> keyFilter == null || keyFilter.test(entry.getKey(), entry.getValue()))
//                .toList();
//        }
//    }

    public boolean isTemporaryObject(ProviderCacheKeyCommon key) {
        synchronized (cacheMap) {
            return get(key) instanceof TemporaryCacheObject;
        }
    }

    public boolean isTemporaryExpired(ProviderCacheKeyCommon key) {
        synchronized (cacheMap) {
            return get(key) instanceof TemporaryCacheObject<?> tempCacheObject && tempCacheObject.isExpired();
        }
    }

    public @Nullable Time getTemporaryTimeToLive(ProviderCacheKeyCommon key) {
        synchronized (cacheMap) {
            return ifNotNull((TemporaryCacheObject) get(key), TemporaryCacheObject::getTimeToLive);
        }
    }

    public <X extends Exception> V getOrPut(ProviderCacheKey key, ThrowingSupplier<V, X> supplier) throws X {
        synchronized (cacheMap) {
            if (contains(key)) {
                return get(key);
            }
        }
        V value = supplier.get();
        ExpiringCacheObject<V> obj = new ExpiringCacheObject<>(value);
        put(key, obj);
        return value;
    }

    public void remove(ProviderCacheKeyCommon key) {
        synchronized (cacheMap) {
            switch (key) {
                case ProviderCacheKey k -> {
                    cacheMap.remove(k);
                    keyMapperCacheInverse.get(k).forEach(keyMapperCache::remove);
                }
                case ProviderCacheKeySub k -> keyMapperCache.remove(k);
            }
        }
    }

    public int size() {
        synchronized (cacheMap) {
            return cacheMap.size();
        }
    }

    public List<Pair<ProviderCacheKey, V>> getEntries(@Nullable Predicate<ProviderCacheKey> keyFilter=null) {
        synchronized (cacheMap) {
            return getEntryStream(keyFilter).map(entry -> Pair.of(entry.getKey(), entry.getValue().value)).toList();
        }
    }

    public Stream<Entry<ProviderCacheKey, CacheObject<V>>> getEntryStream(
        @Nullable Predicate<ProviderCacheKey> keyFilter=null) {
        synchronized (cacheMap) {
            return cacheMap.entrySet().stream().filter(entry -> keyFilter == null || keyFilter.test(entry.getKey()));
        }
    }

    public void deleteEntries(Predicate<ProviderCacheKey> keyFilter) {
        synchronized (cacheMap) {
            getEntryStream(keyFilter).toList().forEach(entry -> remove(entry.getKey()));
        }
    }

    public void cleanup() {
        cleanup(null);
    }

    public void cleanup(@Nullable BiPredicate<ProviderCacheKey, CacheObject<V>> keyFilter) {
        synchronized (cacheMap) {
            Iterator<Entry<ProviderCacheKey, CacheObject<V>>> itr = cacheMap.entrySet().iterator();
            while (itr.hasNext()) {
                Entry<ProviderCacheKey, CacheObject<V>> entry = itr.next();
                if ((keyFilter == null || keyFilter.test(entry.getKey(), entry.getValue()))) {
                    itr.remove();
                    removeFromCache(entry.getKey());
                }
            }
            Thread.yield();
        }
    }

    protected abstract void removeFromCache(ProviderCacheKey key);
}

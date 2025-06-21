package org.lodder.subtools.sublibrary.cache;

import static manifold.ext.props.rt.api.PropOption.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
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
public abstract sealed class ProviderCache permits ProviderCacheMemory, ProviderCacheDisk {

    @val(Protected) final Map<ProviderCacheKey, CacheObject> cacheMap;
    @val(Protected) final Map<ProviderCacheKeySub, ProviderCacheKey> keyMapperCache = new HashMap<>();
    @val(Protected) final Multimap<ProviderCacheKey, ProviderCacheKeySub> keyMapperCacheInverse =
        MultimapBuilder.hashKeys().arrayListValues().build();
    @val(Protected) final Set<ProviderCacheKeySub> invalidKeys = new HashSet<>();

    protected ProviderCache(@Nullable Integer maxItems=null) {
        this.cacheMap = maxItems != null ? new LRUMap<>(maxItems) : new HashMap<>();
    }

    public void put(ProviderCacheKey key, @Nullable Object value, @Nullable Time timeToLive=null) {
        if (timeToLive == null) {
            put(key, new ExpiringCacheObject(value));
        } else {
            put(key, new TemporaryCacheObject(timeToLive, value));
        }
    }

    protected void put(ProviderCacheKey key, CacheObject value) {
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
                    Optional.ofNullable(keyMapperCache.get(k)).map(cacheMap::containsKey).orElse(false);
            };
        }
    }

    public Optional<Object> get(ProviderCacheKeyCommon key) {
        synchronized (cacheMap) {
            CacheObject cacheObject = switch (key) {
                case ProviderCacheKey k -> cacheMap.get(k);
                case ProviderCacheKeySub k -> invalidKeys.contains(k) ? null :
                    Optional.ofNullable(keyMapperCache.get(k)).map(cacheMap::get).orElse(null);
            };
            return Optional.ofNullable(cacheObject)
                .map(obj -> {
                    obj.updateLastAccessed();
                    return obj.value;
                });
        }
    }

    public boolean isTemporaryObject(ProviderCacheKeyCommon key) {
        synchronized (cacheMap) {
            return get(key).map(v -> v instanceof TemporaryCacheObject).orElse(false);
        }
    }

    public boolean isTemporaryExpired(ProviderCacheKeyCommon key) {
        synchronized (cacheMap) {
            return get(key).map(v -> v instanceof TemporaryCacheObject tempCacheObject && tempCacheObject.isExpired())
                .orElse(false);
        }
    }

    public Optional<Time> getTemporaryTimeToLive(ProviderCacheKeyCommon key) {
        synchronized (cacheMap) {
            return get(key).map(v -> v instanceof TemporaryCacheObject t ? t.timeToLive : null);
        }
    }

    public <X extends Exception> @Nullable Object getOrPut(ProviderCacheKey key,
        ThrowingSupplier<Object, X> supplier) throws X {
        synchronized (cacheMap) {
            if (contains(key)) {
                return get(key);
            } else {
                Object value = supplier.get();
                ExpiringCacheObject obj = new ExpiringCacheObject(value);
                put(key, obj);
                return value;
            }
        }
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

    public List<Pair<ProviderCacheKey, Object>> getEntries(@Nullable Predicate<ProviderCacheKey> keyFilter=null) {
        synchronized (cacheMap) {
            return getEntryStream(keyFilter).map(entry -> Pair.of(entry.getKey(), entry.getValue().value)).toList();
        }
    }

    public Stream<Entry<ProviderCacheKey, CacheObject>> getEntryStream(
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

    public abstract void cleanup(@Nullable Predicate<ProviderCacheKey> keyFilter);

}

package org.lodder.subtools.sublibrary.cache;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class LRUMap<K, V> extends LinkedHashMap<K, V> {
    @Serial
    private static final long serialVersionUID = 1L;
    private final int maxItems;

    public LRUMap(int maxItems) {
        this.maxItems = maxItems;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxItems;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof LRUMap lruMap && super.equals(lruMap)
            && maxItems == lruMap.maxItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), maxItems);
    }
}

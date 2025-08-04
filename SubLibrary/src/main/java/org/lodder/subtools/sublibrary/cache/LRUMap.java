package org.lodder.subtools.sublibrary.cache;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;

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
    public int hashCode() {
        return maxItems + super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof LRUMap map && this.maxItems == map.maxItems && super.equals(map);
    }
}

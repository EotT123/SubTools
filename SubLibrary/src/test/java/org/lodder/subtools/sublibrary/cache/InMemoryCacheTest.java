package org.lodder.subtools.sublibrary.cache;

import static manifold.science.util.UnitConstants.*;
import static org.assertj.core.api.Assertions.*;
import static org.lodder.subtools.sublibrary.util.Sleep.*;

import java.util.List;

import manifold.science.measures.Time;
import org.junit.jupiter.api.Test;

class InMemoryCacheTest {

    @Test
    void testAddRemoveObjects() {
        ProviderCacheMemory cache = new ProviderCacheMemory<>(maxItems:6);

        cache.put(new ProviderCacheKey("provider", "eBay"), "eBay");
        cache.put(new ProviderCacheKey("provider", "Paypal"), "Paypal");
        cache.put(new ProviderCacheKey("provider", "Google"), "Google");
        cache.put(new ProviderCacheKey("provider", "Microsoft"), "Microsoft");
        cache.put(new ProviderCacheKey("provider", "IBM"), "IBM");
        cache.put(new ProviderCacheKey("provider", "Facebook"), "Facebook");

        assertThat(cache.size()).as("Cache should contain 6 entries").isEqualTo(6);
        cache.remove(new ProviderCacheKey("provider", "IBM"));
        assertThat(cache.size()).as("After deletion, cache should contain 5 entries").isEqualTo(5);

        cache.put(new ProviderCacheKey("provider", "Twitter"), "Twitter");
        cache.put(new ProviderCacheKey("provider", "SAP"), "SAP");

        assertThat(cache.size()).as("Cache should not contain more elements than it max defined size").isEqualTo(6);
    }

    @Test
    void testExpiredCacheObjects() {

        ProviderCacheMemory cache = new ProviderCacheMemory<>(
            timeToLive:1ms, timerInterval:1000ms, maxItems:10);

        cache.put(new ProviderCacheKey("provider", "eBay"), "eBay");
        cache.put(new ProviderCacheKey("provider", "Paypal"), "Paypal");
        // Adding 3 seconds sleep.. Both above objects will be removed from
        // Cache because of timeToLiveInSeconds value

        sleep(3s);

        assertThat(cache.size()).as("Cache should not contain items that are expired").isEqualTo(0);
    }

    @Test
    void testObjectsCleanupTime() {
        int size = 500;

        ProviderCacheMemory cache = new ProviderCacheMemory<>(
            timeToLive:100s, timerInterval:100ms, maxItems:500);

        for (int i = 0; i < size; i++) {
            String value = Integer.toString(i);
            cache.put(new ProviderCacheKey("provider", value), value);
        }

        sleep(200ms);

        Time start = Time.now();
        cache.cleanup();
        Time duration = Time.now() - start;

        System.out.println("Cleanup duration for $size objects is $duration");
    }

    @Test void testObjectsAndSubObjects() {
        ProviderCacheMemory<String> cache = new ProviderCacheMemory<>();

        ProviderCacheKeyParam cacheKeyParam1 = new ProviderCacheKeyParam("paramkey1", "paramvalue1");
        ProviderCacheKeyParam cacheKeyParam2 = new ProviderCacheKeyParam("paramkey2", "paramvalue2");
        ProviderCacheKeyParam cacheKeyParam3 = new ProviderCacheKeyParam("paramkey3", "paramvalue3");
        ProviderCacheKeyParam cacheKeyParam4 = new ProviderCacheKeyParam("paramkey4", "paramvalue4");

        cache.put(new ProviderCacheKey("provider", "type", List.of(cacheKeyParam1, cacheKeyParam2, cacheKeyParam3)),
            "value");

        assertThat(cache.size()).as("Cache should contain 1 entry").isEqualTo(1);
        assertThat(cache.get(new ProviderCacheKey("provider", "type",
            List.of(cacheKeyParam1, cacheKeyParam2, cacheKeyParam3)))).contains("value");
        // sub keys should also be present
        assertThat(cache.get(new ProviderCacheKeySub("provider", "type", cacheKeyParam1))).contains("value");
        assertThat(cache.get(new ProviderCacheKeySub("provider", "type", cacheKeyParam2))).contains("value");
        assertThat(cache.get(new ProviderCacheKeySub("provider", "type", cacheKeyParam3))).contains("value");

        // add new value with duplicated sub key
        cache.put(new ProviderCacheKey("provider", "type", List.of(cacheKeyParam4, cacheKeyParam3)), "value2");

        assertThat(cache.size()).as("Cache should contain 2 entries").isEqualTo(2);
        // sub keys should also be present
        assertThat(cache.get(new ProviderCacheKeySub("provider", "type", cacheKeyParam1))).contains("value");
        assertThat(cache.get(new ProviderCacheKeySub("provider", "type", cacheKeyParam2))).contains("value");
        assertThat(cache.get(new ProviderCacheKeySub("provider", "type", cacheKeyParam4))).contains("value2");

        // duplicated sub key should not be present
        assertThat(cache.get(new ProviderCacheKeySub("provider", "type", cacheKeyParam3))).isEmpty();

        // Remove 2nd value
        cache.remove(new ProviderCacheKey("provider", "type", List.of(cacheKeyParam4, cacheKeyParam3)));

        assertThat(cache.size()).as("Cache should contain 1 entry").isEqualTo(1);
        // sub keys of remaining value should be present
        assertThat(cache.get(new ProviderCacheKeySub("provider", "type", cacheKeyParam1))).contains("value");
        assertThat(cache.get(new ProviderCacheKeySub("provider", "type", cacheKeyParam2))).contains("value");
        // duplicated subkey should not be present
        assertThat(cache.get(new ProviderCacheKeySub("provider", "type", cacheKeyParam4))).isEmpty();
        // subkey of removed value should not be present
        assertThat(cache.get(new ProviderCacheKeySub("provider", "type", cacheKeyParam4))).isEmpty();

        // Cleanup by filter
        cache.cleanup((cacheKey, cacheValue) -> "provider".equals(cacheKey.provider));
        assertThat(cache.size()).as("Cache should be empty").isEqualTo(0);

        // sub keys should not be present
        assertThat(cache.get(new ProviderCacheKeySub("provider", "type", cacheKeyParam1))).isEmpty();
        assertThat(cache.get(new ProviderCacheKeySub("provider", "type", cacheKeyParam2))).isEmpty();

    }

}

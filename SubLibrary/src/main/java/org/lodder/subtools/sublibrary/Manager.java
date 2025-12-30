package org.lodder.subtools.sublibrary;

import static manifold.science.measures.TimeUnit.*;
import static manifold.science.util.UnitConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;

import manifold.ext.props.rt.api.val;
import manifold.science.measures.Time;
import name.falgout.jeffrey.throwing.ThrowingConsumer;
import name.falgout.jeffrey.throwing.ThrowingFunction;
import name.falgout.jeffrey.throwing.ThrowingSupplier;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.cache.CacheType;
import org.lodder.subtools.sublibrary.cache.ProviderCache;
import org.lodder.subtools.sublibrary.cache.ProviderCacheDisk;
import org.lodder.subtools.sublibrary.cache.ProviderCacheKey;
import org.lodder.subtools.sublibrary.cache.ProviderCacheKeyParam;
import org.lodder.subtools.sublibrary.cache.ProviderCacheMemory;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import org.lodder.subtools.sublibrary.util.Nothing;
import org.lodder.subtools.sublibrary.util.Sleep;
import org.lodder.subtools.sublibrary.util.http.ApiExceptionIntf;
import org.lodder.subtools.sublibrary.util.http.CookieManager;
import org.lodder.subtools.sublibrary.util.http.HttpClient;
import org.lodder.subtools.sublibrary.util.http.HttpClientException;
import org.lodder.subtools.sublibrary.xml.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

@NullMarked
public class Manager {

    private static final Logger LOGGER = LoggerFactory.getLogger(Manager.class);

    @val HttpClient httpClient;
    @val ProviderCacheMemory<?> inMemoryCache;
    @val ProviderCacheDisk<?> diskCache;

    public Manager(HttpClient httpClient, ProviderCacheMemory<?> inMemoryCache, ProviderCacheDisk<?> diskCache) {
        this.httpClient = httpClient;
        this.inMemoryCache = inMemoryCache;
        this.diskCache = diskCache;
    }

    public void downloadAndExtractFile(String downloadLink, Path file,
        @Nullable ThrowingConsumer<String, IOException> validateFunction=null) throws IOException {
        try {
            httpClient.downloadAndExtractFile(new URI(downloadLink).toURL(), file, validateFunction);
            if (!Files.exists(file)) {
                throw new IOException("Could not download subtitle");
            } else if (Files.size(file) == 0) {
                // file is empty, delete it
                Files.delete(file);
                throw new IOException("Downloaded subtitle is empty");
            }
        } catch (MalformedURLException | URISyntaxException e) {
            throw new IOException("Invalid url: " + downloadLink, e);
        }
    }

    public void storeCookies(String domain, Map<String, String> cookieMap) {
        httpClient.storeCookies(domain, cookieMap);
    }

    // ==== \\
    // POST \\
    // ==== \\

    public PostBuilder postBuilder(String url, @Nullable String userAgent=null) {
        return new PostBuilder(httpClient, url, userAgent);
    }

    @NullMarked
    public static class PostBuilder {
        @val HttpClient httpClient;
        @val String url;
        @val @Nullable String userAgent;
        private final Map<String, String> data = new HashMap<>();

        public PostBuilder(HttpClient httpClient, String url, @Nullable String userAgent=null) {
            this.httpClient = httpClient;
            this.url = url;
            this.userAgent = userAgent;
        }

        public PostBuilder addData(String key, String value) {
            data.put(key, value);
            return this;
        }

        public String post() throws ManagerException {
            try {
                return httpClient.doPost(new URI(url).toURL(), userAgent, data);
            } catch (MalformedURLException | URISyntaxException e) {
                throw new ManagerException("incorrect url", e);
            } catch (HttpClientException e) {
                throw new ManagerException(e);
            }
        }

        public org.jsoup.nodes.Document postAsJsoupDocument() throws ManagerException {
            return Jsoup.parse(post());
        }
    }

    // ================ \\
    // GET PAGE CONTENT \\
    // ================ \\

    public String get(PageContentParams params) throws ManagerException {
        return switch (params.cacheType) {
            case NONE -> getContentWithoutCache(params);
            case MEMORY -> ((ProviderCacheMemory<String>) inMemoryCache).getOrPut(
                new ProviderCacheKey("pagecontent", params.url + params.cookieManager(), List.of()),
                () -> getContentWithoutCache(params));
            case DISK -> throw new IllegalArgumentException("Unexpected value: " + params.cacheType);
        };
    }

    public InputStream getAsInputStream(PageContentParams params) throws ManagerException {
        return get(params).toInputStream(StandardCharsets.UTF_8);
    }

    public @Nullable Document getAsDocument(PageContentParams params,
        @Nullable Predicate<String> emptyResultPredicate=null)
        throws ManagerException, IOException {
        return getAsStringDocument(params, emptyResultPredicate).mapEx(XMLHelper::getDocument).orElse(null);
    }

    public org.jsoup.nodes.@Nullable Document getAsJsoupDocument(PageContentParams params,
        @Nullable Predicate<String> emptyResultPredicate=null) throws ManagerException {
        return getAsStringDocument(params, emptyResultPredicate).map(Jsoup::parse).orElse(null);
    }

    private Optional<String> getAsStringDocument(PageContentParams params,
        @Nullable Predicate<String> emptyResultPredicate) throws ManagerException {
        if (emptyResultPredicate == null) {
            return Optional.of(get(params));
        }
        String html = get(params);
        return StringUtils.isBlank(html) || emptyResultPredicate.test(html) ? Optional.empty() : Optional.of(html);
    }

    public JSONObject getAsJsonObject(PageContentParams params) throws ManagerException {
        return new JSONObject(getAsJsonString(params));
    }

    public JSONArray getAsJsonArray(PageContentParams params) throws ManagerException {
        return new JSONArray(getAsJsonString(params));
    }

    private String getAsJsonString(PageContentParams params) throws ManagerException {
        try {
            return new String(getAsInputStream(params).readAllBytes(), StandardCharsets.UTF_8);
        } catch (JSONException | IOException | ManagerException e) {
            throw new ManagerException(e);
        }
    }

    private String getContentWithoutCache(PageContentParams params) throws ManagerException {
        return getContentWithoutCache(params.url, params.userAgent, params.retry, params.cookieManager);
    }

    private String getContentWithoutCache(String url, String userAgent, Retry retry,
        @Nullable CookieManager cookieManager) throws ManagerException {
        try {
            return httpClient.doGet(new URI(url).toURL(), userAgent, cookieManager);
        } catch (HttpClientException e) {
            if (retry.canRetry() && retry.predicate.test(e)) {
                return getContentWithoutCache(url, userAgent, retry.decreaseRetries().sleep(), cookieManager);
            }
            throw new ManagerException(
                "Error occurred with httpclient response: %s %s while accessing %s".formatted(e.responseCode,
                    e.responseMessage, url), e);
        } catch (IOException e) {
            if (retry.canRetry() && retry.predicate.test(e)) {
                return getContentWithoutCache(url, userAgent, retry.decreaseRetries(), cookieManager);
            }
            throw new ManagerException(e);
        } catch (URISyntaxException e) {
            throw new ManagerException("Invalid url [%s]".formatted(url), e);
        }
    }

    @NullMarked
    public record Retry(int retries, Predicate<Exception> predicate, Time waitTime) {

        public static final Retry NONE = new Retry(0, _ -> false, 0Second);

        public Retry {
            if (retries < 0) {
                throw new IllegalStateException("Number of retries cannot be less than 0");
            }
        }

        public Retry decreaseRetries() {
            return new Retry(retries - 1, predicate, waitTime);
        }

        public boolean canRetry() {
            return retries > 0;
        }

        public Retry sleep() {
            Sleep.sleep(waitTime);
            return this;
        }
    }

    public <V> List<Pair<ProviderCacheKey, V>> getEntries(CacheType cacheType,
        Predicate<ProviderCacheKey> keyFilter, @Nullable Class<V> type=null) {
        return getCache(cacheType, keyFilter).getEntries(type);
    }


    // ============= \\
    // CACHE METHODS \\
    // ============= \\

    @NullMarked
    public record CacheKey(Manager manager, CacheType cacheType, ProviderCacheKey key) {

        public boolean isPresent() {
            return manager.getOptionalCache(cacheType).map(cache -> cache.contains(key)).orElse(false);
        }

        public boolean isNotPresent() {
            return !isPresent();
        }

        public boolean isExpiredTemporary() {
            return manager.getOptionalCache(cacheType).map(cache -> cache.isTemporaryExpired(key)).orElse(false);
        }

        public boolean isTemporaryObject() {
            return manager.getOptionalCache(cacheType).map(cache -> cache.isTemporaryObject(key)).orElse(false);
        }

        public Optional<Time> getTemporaryTimeToLive() {
            return manager.getOptionalCache(cacheType).map(cache -> cache.getTemporaryTimeToLive(key))
                .orElseGet(() -> Optional.of(0 Second));
        }

        public void remove() {
            manager.getCache(cacheType).remove(key);
        }

        public <V extends @Nullable Object, X extends Exception> V get(ThrowingSupplier<V, X> supplier,
            @Nullable Time timeToLive=null, Retry retry=Retry.NONE) throws X {
            ProviderCache<V> cache = manager.getCache(cacheType);
            if (cache.contains(key) && !cache.isTemporaryExpired(key)) {
                return cache.get(key).orElseThrow();
            }
            try {
                V value = executeSupplier(supplier, retry);
                if (value != null) {
                    cache.put(key, value, timeToLive);
                } else {
                    switch (cache) {
                        case ProviderCacheDisk<V> dCache -> dCache.putWithoutPersist(key, null);
                        case ProviderCacheMemory<V> mCache -> mCache.put(key, null);
                    }
                }
                return value;
            } catch (Exception ex) {
                if (ex instanceof ApiExceptionIntf e) {
                    LOGGER.log(e.logLevel, ex.getMessage(), ex);
                    switch (e.cacheStrategy) {
                        case CACHE_DISABLED -> {
                        }
                        case CACHE_TEMPORARY -> storeTempValue(Value.of(List.of()));
                        case CACHE_PERMANENT -> store(Value.of(List.of()));
                    }
                }
                throw (X) ex;
            }
        }

        public <C extends Iterable<? extends V>, V, X extends Exception> C getCollection(
            ThrowingSupplier<C, X> supplier, Retry retry=Retry.NONE) throws X {
            Optional<ProviderCache<C>> optionalCache = manager.getOptionalCache(cacheType);
            return optionalCache.mapEx(cache -> {
                if (cache.contains(key)) {
                    return cache.get(key).orElseThrow();
                }
                try {
                    C value = executeSupplier(supplier, retry);
                    cache.put(key, value);
                    return value;
                } catch (Exception ex) {
                    if (ex instanceof ApiExceptionIntf e) {
                        LOGGER.log(e.logLevel, ex.getMessage(), ex);
                        switch (e.cacheStrategy) {
                            case CACHE_DISABLED -> {
                            }
                            case CACHE_TEMPORARY -> storeTempValue(Value.of(List.of()));
                            case CACHE_PERMANENT -> store(Value.of(List.of()));
                        }
                    }
                    throw (X) ex;
                }
            }).orElseGetEx(supplier);
        }

        public <V> Optional<V> getOptional() {
            Optional<ProviderCache<V>> optionalCache = manager.getOptionalCache(cacheType);
            return optionalCache.flatMap(cache -> (Optional<V>) cache.get(key));
        }

        public <V extends @Nullable Object, X extends Exception> Optional<V> getOptional(
            ThrowingSupplier<Optional<V>, X> supplier, @Nullable Time timeToLive=null, Retry retry=Retry.NONE,
            boolean storeTempNullValue=false) throws X {
            Optional<ProviderCache<V>> optionalCache = manager.getOptionalCache(cacheType);

            if (optionalCache.isPresent()) {
                ProviderCache<V> cache = optionalCache.get();
                boolean containsKey = cache.contains(key);
                if (!containsKey && storeTempNullValue) {
                    store(Value.ofOptional(supplier), storeTempNullValue, timeToLive, retry);
                    return cache.get(key);
                } else if (containsKey && !isExpiredTemporary()) {
                    return cache.get(key);
                } else {
                    try {
                        Optional<V> object = executeSupplier(supplier, retry);
                        object.ifPresentOrElse(v -> cache.put(key, v, timeToLive), () -> {
                            switch (cache) {
                                case ProviderCacheDisk<V> dCache -> dCache.putWithoutPersist(key, null);
                                case ProviderCacheMemory<V> mCache -> mCache.put(key, null);
                            }
                        });
                        return object;
                    } catch (Exception ex) {
                        if (ex instanceof ApiExceptionIntf e) {
                            LOGGER.log(e.logLevel, ex.getMessage(), ex);
                            switch (e.cacheStrategy) {
                                case CACHE_DISABLED -> {
                                }
                                case CACHE_TEMPORARY -> storeTempValue(Value.ofOptional(Optional.empty()));
                                case CACHE_PERMANENT -> store(Value.ofOptional(Optional.empty()));
                            }
                        }
                        throw (X) ex;
                    }
                }
            } else {
                return supplier.get();
            }
        }

        public <X extends Exception> OptionalInt getOptionalInt(
            ThrowingSupplier<OptionalInt, X> supplier, @Nullable Time timeToLive=null, Retry retry=Retry.NONE,
            boolean storeTempNullValue=false) throws X {

            return manager.getOptionalCache(cacheType).mapEx(cache -> {
                boolean containsKey = cache.contains(key);
                if (!containsKey && storeTempNullValue) {
                    store(Value.ofOptionalInt(supplier), storeTempNullValue, timeToLive, retry);
                    return cache.get(key).mapToIntEx(t -> (int) t);
                } else if (containsKey && !isExpiredTemporary()) {
                    return cache.get(key).mapToIntEx(t -> (int) t);
                } else {
                    try {
                        OptionalInt object = executeSupplier(supplier, retry);
                        object.ifPresentOrElse(v -> cache.put(key, v), () -> {
                            switch (cache) {
                                case ProviderCacheDisk<? extends @Nullable Object> dCache ->
                                    dCache.putWithoutPersist(key, null);
                                case ProviderCacheMemory<? extends @Nullable Object> mCache -> mCache.put(key, null);
                            }
                        });
                        return object;
                    } catch (Exception ex) {
                        if (ex instanceof ApiExceptionIntf e) {
                            LOGGER.log(e.logLevel, ex.getMessage(), ex);
                            switch (e.cacheStrategy) {
                                case CACHE_DISABLED -> {
                                }
                                case CACHE_TEMPORARY -> storeTempValue(Value.ofOptionalInt(OptionalInt.empty()));
                                case CACHE_PERMANENT -> store(Value.ofOptionalInt(OptionalInt.empty()));
                            }
                        }
                        throw (X) ex;
                    }
                }
            }).orElseGetEx(supplier);
        }

        public <V extends @Nullable Object, X extends Exception> void store(Value<V, X> value,
            boolean storeTempNullValue=false, @Nullable Time timeToLive=null, Retry retry=Retry.NONE) throws X {

            V object = value.getValue(retry);
            Time ttl = storeTempNullValue && object == null ? getTemporaryTimeToLive().orElse(12 hr) * 2 : timeToLive;
            manager.getCache(cacheType).put(key, object, ttl);
        }

        public <V, X extends Exception> void storeTempValue(Value<V, X> value) throws X {
            Time ttl = getTemporaryTimeToLive().orElse(1 hr) * 2;
            manager.getCache(cacheType).put(key, value.getValue(), ttl);
        }
    }

    @NullMarked
    public record CacheKeyFilter(Manager manager, CacheType cacheType, Predicate<ProviderCacheKey> keyFilter) {
        public void clearExpiredCache() {
            Time now = Time.now();
            manager.getCache(cacheType).cleanup((key, cacheValue) -> keyFilter.test(key) && cacheValue.isExpired(now));
        }

        public void remove() {
            manager.getCache(cacheType).deleteEntries(keyFilter);
        }

        public <V> List<Pair<ProviderCacheKey, V>> getEntries(@Nullable Class<V> valueType=null) {
            Optional<ProviderCache<V>> optionalCache = manager.getOptionalCache(cacheType);
            return optionalCache.map(cache -> cache.getEntries(keyFilter)).orElseGet(List::of);
        }
    }

    private <V> ProviderCache<V> getCache(CacheType cacheType) {
        Optional<ProviderCache<V>> optionalCache = getOptionalCache(cacheType);
        return optionalCache.orElseThrow(() -> new IllegalArgumentException("Unexpected value: " + cacheType));
    }

    private <V> Optional<ProviderCache<@Nullable V>> getOptionalCache(CacheType cacheType) {
        return switch (cacheType) {
            case NONE -> Optional.empty();
            case MEMORY -> (Optional) Optional.of(inMemoryCache);
            case DISK -> (Optional) Optional.of(diskCache);
        };
    }

    public CacheKey getCache(CacheType cacheType, CacheKeyBuilder cacheKeyBuilder) {
        return new CacheKey(this, cacheType, cacheKeyBuilder.build());
    }

    public CacheKeyFilter getCache(CacheType cacheType, Predicate<ProviderCacheKey> keyFilter) {
        return new CacheKeyFilter(this, cacheType, keyFilter);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @NullMarked
    public record Value<V extends @Nullable Object, X extends Exception>(ThrowingFunction<Retry, V, X> supplier) {
        public static <V> Value<V, Nothing> of(V value) {
            return new Value<>(_ -> value);
        }

        public static <V, X extends Exception> Value<V, X> of(ThrowingSupplier<V, X> supplier) {
            return new Value<>(retry -> executeSupplier(supplier, retry));
        }

        public static <V extends @Nullable Object> Value<V, Nothing> ofOptional(Optional<V> value) {
            return new Value<@Nullable V, Nothing>(_ -> value.orElse(null));
        }

        public static <V extends @Nullable Object, X extends Exception> Value<V, X> ofOptional(
            ThrowingSupplier<Optional<V>, X> supplier) {
            return new Value<@Nullable V, X>(retry -> executeSupplier(supplier, retry).orElse(null));
        }

        public static Value<@Nullable Integer, Nothing> ofOptionalInt(OptionalInt value) {
            return ofOptional(() -> value.mapToObj(i -> i));
        }

        public static <X extends Exception> Value<@Nullable Integer, X> ofOptionalInt(
            ThrowingSupplier<OptionalInt, X> supplier) {
            return ofOptional(() -> supplier.get().mapToObj(i -> i));
        }

        public static <C extends Collection<V>, V> Value<C, Nothing> ofCollection(C value) {
            return new Value<>(_ -> value);
        }

        public static <C extends Collection<V>, V, X extends Exception> Value<C, X> ofCollection(ThrowingSupplier<C,
            X> supplier) {
            return new Value<>(retry -> executeSupplier(supplier, retry));
        }

        public V getValue(Retry retry=Retry.NONE) throws X {
            return supplier.apply(retry);
        }
    }

    @NullMarked
    public static class CacheKeyBuilder {
        @val String source;
        @val String operation;
        //        private final Map<String, Object> extraParams = new LinkedHashMap<>();
        private final List<ProviderCacheKeyParam> idParams = new ArrayList<>();
        private final List<ProviderCacheKeyParam> params = new ArrayList<>();

        public CacheKeyBuilder(SubtitleSource source, String operation) {
            this(source.name, operation);
        }

        public CacheKeyBuilder(String source, String operation) {
            this.source = source;
            this.operation = operation;
        }

        public CacheKeyBuilder addIdParam(String name, @Nullable Object value) {
            idParams.add(new ProviderCacheKeyParam(name, value));
            return this;
        }

        public CacheKeyBuilder add(String name, @Nullable Object value) {
            params.add(new ProviderCacheKeyParam(name, value));
            return this;
        }

        public CacheKeyBuilder add(List<ProviderCacheKeyParam> params) {
            this.params.addAll(params);
//            params.entrySet().stream().sorted(Comparator.comparing(e -> String.valueOf(e.getKey())))
//                .forEach(entry -> add(String.valueOf(entry.getKey()), entry.getValue()));
            return this;
        }

        public ProviderCacheKey build() {
            return new ProviderCacheKey(source, operation, idParams, params);
        }

    }

    // ############## \\
    // HELPER METHODS \\
    // ############## \\

    @SuppressWarnings("unchecked")
    private static <V extends @Nullable Object, X extends Exception> V executeSupplier(ThrowingSupplier<V, X> supplier,
        Retry retry) throws X {
        try {
            return supplier.get();
        } catch (Exception e) {
            if (retry.canRetry() && retry.predicate.test(e)) {
                return executeSupplier(supplier, retry.decreaseRetries().sleep());
            }
            throw (X) e;
        }
    }
}

package org.lodder.subtools.sublibrary.cache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.gsonfire.GsonFireBuilder;
import manifold.science.measures.Time;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;
import org.lodder.subtools.sublibrary.util.lazy.LazyBiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public final class ProviderCacheDisk<V> extends ProviderCache<V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderCacheDisk.class);
    private static final Object LOCK = new Object();
    private static final Gson GSON = new GsonBuilder().create();

    private final @Nullable Time timeToLive;
    private final Set<ProviderCacheKey> doublesToRemove = new HashSet<>();
    private final Map<ProviderCacheKey, CacheObject<V>> removedToAdd = new HashMap<>();
    private final LazyBiFunction<ProviderCacheDisk<V>, String, Connection>
        connection =
        new LazyBiFunction<>((cache, tableName) -> {
            try {
                synchronized (cache.cacheMap) {
                    Path path = Path.of(System.getProperty("user.home")).resolve(".MultiSubDownloader");
                    if (!Files.exists(path)) {
                        try {
                            Files.createDirectory(path);
                        } catch (IOException e) {
                            throw new RuntimeException("Could not create folder $path", e);
                        }
                    }
                    Class.forName("org.hsqldb.jdbcDriver");
                    Connection connection = DriverManager.getConnection(
                        "jdbc:hsqldb:file:$path/diskcache.hsqldb;hsqldb.write_delay=false;shutdown=true",
                        "user", "pass");

                    try (Statement stmt = connection.createStatement()) {
                        stmt.execute("create table IF NOT EXISTS $tableName (key VARCHAR(32768), cacheobject OBJECT);");
                    }

                    boolean errorWhileReadingCacheFile = false;
                    try (Statement stmt = connection.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT key, cacheobject FROM $tableName;")) {
                        Multimap<ProviderCacheKey, CacheObject<V>> tempCache = MultimapBuilder.hashKeys()
                            .treeSetValues(Comparator.comparing((CacheObject<V> value) -> value.age).reversed())
                            .build();
                        Gson gson = new GsonFireBuilder().enableHooks(SerieMapping.class).createGson();
                        synchronized (cache.cacheMap) {
                            while (rs.next()) {
                                try {
                                    tempCache.put(gson.fromJson((String) rs.getObject("key"), ProviderCacheKey.class),
                                        (CacheObject<V>) rs.getObject("cacheobject"));
                                } catch (SQLException e2) {
                                    LOGGER.error("Unable to insert object in disk cache. (${e2.getMessage()})", e2);
                                    errorWhileReadingCacheFile = true;
//                                    throw new CorruptSettingsFileException(e);
                                }
                            }
                            Map<ProviderCacheKey, Collection<CacheObject<V>>> map = tempCache.asMap();
                            map.entrySet().stream()
                                .filter(entry -> entry.getValue().size() > 1)
                                .forEach(entry -> {
                                    doublesToRemove.add(entry.getKey());
                                    removedToAdd.put(entry.getKey(), entry.getValue().last());
                                });
                            map.entrySet()
                                .stream()
                                .sorted(Comparator.comparing(entry -> entry.getValue().iterator().next().age))
                                .forEach(entry -> put(entry.getKey(), entry.getValue().iterator().next()));
                        }
                    } catch (SQLException e) {
                        LOGGER.error("Unable while insert objects in disk cache! (${e.getMessage()})", e);
                    }
                    if (errorWhileReadingCacheFile) {
                        LOGGER.error("Deleting cache file to fix errors");
                        connection.close();
                        try {
                            path.deletePath();
                        } catch (IOException e) {
                            LOGGER.error("Error while deleting the cache file, please delete it yourself: $path " +
                                "(${e.getMessage()})", e);
                        }
                        connection = DriverManager.getConnection(
                            "jdbc:hsqldb:file:$path/diskcache.hsqldb;hsqldb.write_delay=false;shutdown=true",
                            "user", "pass");
                    }
                    return connection;
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Unable to load jdbcdriver for diskcache");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    private final String tableName;

    @NullMarked
    public ProviderCacheDisk(
        @Nullable Time timeToLive=null,
        @Nullable Integer maxItems=null,
        @Nullable String tableName=null) {

        super(maxItems);
        if (timeToLive != null && timeToLive.isNegative()) {
            throw new IllegalStateException("timeToLive should be a positive number");
        }
        this.timeToLive = timeToLive;
        this.tableName = StringUtils.isBlank(tableName) ? "cacheobjects" : tableName;
        // initialize map in other thread
        new Thread(() -> {
            getConnection();
            doublesToRemove.forEach(this::remove);
            removedToAdd.forEach(this::put);
        }).start();
    }

    private Connection getConnection() {
        return connection.apply(this, tableName);
    }

    protected void removeFromCache(ProviderCacheKey key) {
        removeFromDisk(key);
    }

    @Override
    public void remove(ProviderCacheKeyCommon key) {
        super.remove(key);
        synchronized (LOCK) {
            removeFromDisk(key);
        }
    }

    private void removeFromDisk(ProviderCacheKeyCommon key) {
        synchronized (LOCK) {
            try (PreparedStatement prep = getConnection().prepareStatement("delete from $tableName where key = ?")) {
                prep.setObject(1, GSON.toJson(key));
                prep.executeUpdate();
            } catch (SQLException e) {
                LOGGER.error("Unable to delete object from disk cache!", e);
            }
        }
    }

    @Override
    public void put(ProviderCacheKey key, @Nullable V value, @Nullable Time timeToLive) {
        synchronized (LOCK) {
            super.put(key, value, timeToLive);
            putFromMemoryCache(key);
        }
    }

    private void putFromMemoryCache(ProviderCacheKey key) {
        synchronized (LOCK) {
            try (PreparedStatement prep = getConnection().prepareCall(
                "INSERT INTO $tableName (key,cacheobject) VALUES (?,?)")) {
                prep.clearParameters();
                prep.setObject(1, GSON.toJson(key));
                synchronized (cacheMap) {
                    CacheObject<V> cacheObject = cacheMap.get(key);
                    prep.setObject(2, cacheObject);
                    prep.execute();
                }
                getConnection().commit();
            } catch (SQLException e) {
                LOGGER.error("Unable to insert object in disk cache!", e);
            }
        }
    }

    public void putWithoutPersist(ProviderCacheKey key, @Nullable V value) {
        super.put(key, value);
    }
}

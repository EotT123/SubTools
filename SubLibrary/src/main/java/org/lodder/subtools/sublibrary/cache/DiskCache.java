package org.lodder.subtools.sublibrary.cache;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang3.StringUtils;
import org.lodder.subtools.sublibrary.util.lazy.LazyBiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DiskCache<K, V> extends InMemoryCache<K, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiskCache.class);
    public static final Object LOCK = new Object();

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private final LazyBiFunction<DiskCache, String, Connection> connection = new LazyBiFunction<>((cache, tableName) -> {
        try {
            synchronized (cache.getCacheMap()) {
                File path = new File(System.getProperty("user.home"), ".MultiSubDownloader");
                if (!path.exists() && !path.mkdir()) {
                    throw new RuntimeException("Could not create folder " + path);
                }
                Class.forName("org.hsqldb.jdbcDriver");
                Connection connection = DriverManager.getConnection(
                        "jdbc:hsqldb:file:" + path.toString() + "/diskcache.hsqldb;hsqldb.write_delay=false;shutdown=true", "user", "pass");

                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("create table IF NOT EXISTS %s (key OTHER, cacheobject OTHER);".formatted(tableName));
                }

                try (Statement stmt = connection.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT key, cacheobject FROM %s;".formatted(tableName));) {
                    synchronized (cache.getCacheMap()) {
                        while (rs.next()) {
                            cache.put(cache.diskObjectToKey(rs.getObject("key")), cache.diskCacheObjectToValue(rs.getObject("cacheobject")));
                        }
                    }
                } catch (SQLException e) {
                    LOGGER.error("Unable to insert object in disk cache!", e);
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

    private Connection getConnection() {
        return connection.apply(this, tableName);
    }

    protected DiskCache(Long timeToLive, Long timerInterval, Integer maxItems, String username, String password, String tableName) {
        super(timeToLive, timerInterval, maxItems);
        this.tableName = StringUtils.isBlank(tableName) ? "cacheobjects" : tableName;
        // initialize map in other thread
        new Thread(() -> getConnection()).start();
        if (timeToLive != null && timeToLive > 0) {
            cleanup();
        }
    }

    protected abstract K diskObjectToKey(Object key);

    protected abstract CacheObject<V> diskCacheObjectToValue(Object value);

    @Override
    public final void remove(K key) {
        super.remove(key);
        synchronized (LOCK) {
            try (PreparedStatement prep = getConnection().prepareCall("delete from %s where key = ?".formatted(tableName))) {
                prep.clearParameters();
                prep.setObject(1, keyToDiskObject(key));
                prep.executeUpdate();
            } catch (SQLException e) {
                LOGGER.error("Unable to delete object from disk cache!", e);
            }
        }
    }

    protected abstract Object keyToDiskObject(K key);

    protected abstract Object cacheObjectToDiskObject(CacheObject<V> value);

    @Override
    public final void put(K key, V value) {
        super.put(key, value);
        synchronized (LOCK) {
            try (PreparedStatement prep = getConnection().prepareCall("INSERT INTO %s (key,cacheobject) VALUES (?,?)".formatted(tableName))) {
                prep.clearParameters();
                prep.setObject(1, keyToDiskObject(key));
                synchronized (getCacheMap()) {
                    CacheObject<V> cacheObject = getCacheMap().get(key);
                    prep.setObject(2, cacheObjectToDiskObject(cacheObject));
                    prep.execute();
                }
                getConnection().commit();
            } catch (SQLException e) {
                LOGGER.error("Unable to insert object in disk cache!", e);
            }
        }
    }

    public void putWithoutPersist(K key, V value) {
        super.put(key, value);
    }
}

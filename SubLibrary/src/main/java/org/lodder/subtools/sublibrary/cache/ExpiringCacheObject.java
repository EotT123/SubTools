package org.lodder.subtools.sublibrary.cache;

import static manifold.ext.props.rt.api.PropOption.*;

import java.io.Serial;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.set;
import manifold.ext.props.rt.api.val;
import manifold.ext.props.rt.api.var;
import manifold.science.measures.Time;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A cache entry that expires after a period of inactivity.
 * <p>
 * The expiration is <em>sliding</em>: each access resets the expiration timer.
 * If the value is not accessed within the configured timeout, it is considered expired.
 *
 * @param <V> the type of the cached value
 */
@NullMarked
public final class ExpiringCacheObject<V extends @Nullable Object> implements CacheObject<V> {

    @Serial
    private static final long serialVersionUID = 1L;

    @override @val Time created;
    @var @set(Private) Time lastAccessed;
    @override @var V value;

    private ExpiringCacheObject(Time created, Time lastAccessed, V value) {
        this.created = created;
        this.lastAccessed = lastAccessed;
        this.value = value;
    }

    ExpiringCacheObject(V value) {
        this(Time.now(), Time.now(), value);
    }

    @Override
    public void updateLastAccessed() {
        lastAccessed = Time.now();
    }

    @Override
    public boolean isExpired(Time ttl) {
        return Time.now().isAfter(lastAccessed + ttl);
    }

    @Override
    public Time getAge() {
        return lastAccessed;
    }
}

package org.lodder.subtools.sublibrary.cache;

import java.io.Serial;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import manifold.science.measures.Time;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A cache entry that expires after a fixed duration.
 * <p>
 * The expiration does not reset on access.
 *
 * @param <V> the type of the cached value
 */
@NullMarked
public final class TemporaryCacheObject<V extends @Nullable Object> implements CacheObject<V> {

    @Serial
    private static final long serialVersionUID = 1L;

    @override @val Time created;
    @val Time timeToLive;
    @override @val V value;

    TemporaryCacheObject(Time timeToLive, V value) {
        this(Time.now(), timeToLive, value);
    }

    private TemporaryCacheObject(Time created, Time timeToLive, V value) {
        this.created = created;
        this.timeToLive = timeToLive;
        this.value = value;
    }

    @Override
    public boolean isExpired(Time ttl) {
        return isExpired();
    }

    public boolean isExpired() {
        return Time.now().isAfter(created + timeToLive);
    }

    @Override
    public void updateLastAccessed() {
        // do nothing
    }

    @Override
    public Time getAge() {
        return created;
    }
}

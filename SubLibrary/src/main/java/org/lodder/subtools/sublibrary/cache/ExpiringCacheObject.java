package org.lodder.subtools.sublibrary.cache;

import static manifold.ext.props.rt.api.PropOption.*;
import static manifold.science.util.UnitConstants.*;

import java.io.Serial;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.set;
import manifold.ext.props.rt.api.val;
import manifold.ext.props.rt.api.var;
import manifold.science.measures.Time;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class ExpiringCacheObject<V extends @Nullable Object> implements CacheObject<V> {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Pattern PATTERN = Pattern.compile("created:(.*?)|lastAccessed:(.*?)|value:(.*)");

    @override @val Time created;
    @var @set(Private) Time lastAccessed = Time.now();
    @override @var V value;

    public ExpiringCacheObject(Time created, Time lastAccessed, V value) {
        this.created = created;
        this.lastAccessed = lastAccessed;
        this.value = value;
    }

    ExpiringCacheObject(V value) {
        this.created = Time.now();
        this.value = value;
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
    public String toString(Function<V, String> valueToStringMapper) {
        return "created:$created|lastAccessed:$lastAccessed|value:${valueToStringMapper.apply(value)}";
    }

    public static <V> Optional<CacheObject<V>> fromString(String string,
        Function<String, V> valueToObjectMapper) {
        Matcher matcher = PATTERN.matcher(string);
        if (matcher.matches()) {
            Time created = Time.create(Long.parseLong(matcher.group(1)), ms);
            Time lastAccessed = Time.create(Long.parseLong(matcher.group(2)), ms);
            String value = matcher.group(3);
            return Optional.of(new ExpiringCacheObject<>(created, lastAccessed, valueToObjectMapper.apply(value)));
        }
        return Optional.empty();
    }

    @Override
    public Time getAge() {
        return lastAccessed;
    }
}

package org.lodder.subtools.sublibrary.cache;

import static manifold.science.util.UnitConstants.*;

import java.io.Serial;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import manifold.science.measures.Time;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class TemporaryCacheObject<V extends @Nullable Object> implements CacheObject<V> {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Pattern PATTERN = Pattern.compile("created:(.*?)|expire:(.*?)|value:(.*)");

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
    public String toString(Function<@Nullable V, String> valueToStringMapper) {
        return "created:%s|expire:%s|value:%s".formatted(created, timeToLive, valueToStringMapper.apply(value));
    }

    public static <V> Optional<TemporaryCacheObject<V>> fromString(String string,
        Function<@Nullable String, V> valueToObjectMapper) {
        Matcher matcher = PATTERN.matcher(string);
        if (matcher.matches()) {
            Time created = Time.create(Long.parseLong(matcher.group(1)), ms);
            Time timeToLive = Time.create(Long.parseLong(matcher.group(2)), ms);
            String value = matcher.group(3);
            return Optional.of(new TemporaryCacheObject<>(created, timeToLive, valueToObjectMapper.apply(value)));
        }
        return Optional.empty();
    }

    @Override
    public Time getAge() {
        return created;
    }
}

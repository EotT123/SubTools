package org.lodder.subtools.sublibrary.cache;

import static manifold.ext.props.rt.api.PropOption.*;
import static org.lodder.subtools.sublibrary.util.Sleep.*;

import manifold.ext.props.rt.api.val;
import manifold.science.measures.Time;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class ProviderCacheMemory<V extends @Nullable Object> extends ProviderCache<V> {

    @val(Protected) @Nullable Time timeToLive;

    public ProviderCacheMemory(@Nullable Time timeToLive=null, @Nullable Time timerInterval=null,
        @Nullable Integer maxItems=null) {
        super(maxItems);
        if (maxItems != null && maxItems < 1) {
            throw new IllegalStateException("maxItems should be a positive number");
        } else if (timerInterval != null && timerInterval.isNegative()) {
            throw new IllegalStateException("timerInterval should be a positive number");
        } else if (timeToLive != null && timeToLive.isNegative()) {
            throw new IllegalStateException("timeToLive should be a positive number");
        } else if (timeToLive == null && timerInterval != null) {
            throw new IllegalStateException("timeToLive should be specified when timerInterval is used");
//        } else if (timeToLive != null && timerInterval != null && timeToLive < timerInterval) {
//            throw new IllegalStateException("timerInterval should be greater than timeToLive");
        }
        if (timerInterval != null) {
            createCleanUpThread(timerInterval, timeToLive);
        }
        this.timeToLive = timeToLive;
    }

    private void createCleanUpThread(Time timerInterval, Time timeToLive) {
        Thread t = new Thread(() -> {
            while (true) {
                sleep(timerInterval);
                cleanup((_, v) -> v.isExpired(timeToLive));
            }
        });

        t.setDaemon(true);
        t.start();
    }

    protected void removeFromCache(ProviderCacheKey key) {
        remove(key);
    }
}

package com.jrobertgardzinski.config.source.live;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TTL decorator over a {@link LiveConfigPort} — the same contract with bounded staleness, which
 * is exactly what keeps the rung LIVE in the promised sense: within the TTL the cached answer is
 * served, ABSENCE included (a vacant rung costs no repeated round trips, and a row's appearance
 * is noticed within one TTL exactly like a change). A zero TTL switches caching off entirely,
 * turning the decorator into plain delegation.
 *
 * <p>Whether a call site reads through this decorator or through the bare adapter is decided
 * where the beans are wired — never in the port's contract, and never by the use case. Each
 * instance caches independently: in a multi-instance deployment every node converges on its own
 * clock, within one TTL of the change.
 */
public final class CachingLiveConfigPort<T> implements LiveConfigPort<T> {

    /** {@code value} may be null — a cached absence is as valid an answer as a cached value. */
    private record CacheEntry<T>(T value, Instant expiresAt) {
    }

    private final LiveConfigPort<T> delegate;
    private final Duration ttl;
    private final Clock clock;
    private final ConcurrentHashMap<String, CacheEntry<T>> cache = new ConcurrentHashMap<>();

    public CachingLiveConfigPort(LiveConfigPort<T> delegate, Duration ttl, Clock clock) {
        if (ttl.isNegative())
            throw new IllegalArgumentException("ttl must not be negative");
        this.delegate = delegate;
        this.ttl = ttl;
        this.clock = clock;
    }

    @Override
    public T find(String name) {
        if (ttl.isZero())
            return delegate.find(name);
        Instant now = clock.instant();
        CacheEntry<T> entry = cache.get(name);
        if (entry == null || !now.isBefore(entry.expiresAt())) {
            entry = new CacheEntry<>(delegate.find(name), now.plus(ttl));
            cache.put(name, entry);
        }
        return entry.value();
    }
}

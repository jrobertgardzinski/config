package com.jrobertgardzinski.config.source.live;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The live level as one snapshot of the whole settings table, taken at most once per TTL: every
 * key is answered from the same read, so a policy of five keys costs one round trip, not five, and
 * every key is live in the same sense — a change, a new row or a deleted one is noticed within
 * one TTL. A zero TTL reads the table on every question.
 *
 * <p>The table is what the snapshot is a copy of, so a write through this instance is followed by
 * {@link #refresh()} and the writer sees their own decision at once; other instances converge
 * within one TTL on their own clocks. When the table cannot be read, the last snapshot stays in
 * force for another TTL and the failure is logged: a stale row is a better answer than a silent
 * fall to the property. Before the first successful read there is nothing to keep, and the level
 * is vacant until the next TTL.
 */
public final class SnapshotLiveConfigPort implements LiveConfigPort<String> {

    private static final System.Logger LOG = System.getLogger(SnapshotLiveConfigPort.class.getName());

    private record Snapshot(Map<String, String> rows, Instant expiresAt) {
    }

    private final Supplier<Map<String, String>> table;
    private final Duration ttl;
    private final Clock clock;
    private volatile Snapshot snapshot;

    /** {@code table} is one read of the whole table: every row, key to text. */
    public SnapshotLiveConfigPort(Supplier<Map<String, String>> table, Duration ttl, Clock clock) {
        if (ttl.isNegative())
            throw new IllegalArgumentException("ttl must not be negative");
        this.table = table;
        this.ttl = ttl;
        this.clock = clock;
    }

    @Override
    public String find(String name) {
        Snapshot current = snapshot;
        Instant now = clock.instant();
        if (current == null || !now.isBefore(current.expiresAt())) {
            current = take(current, now);
            snapshot = current;
        }
        return current.rows().get(name);
    }

    /** Take a fresh snapshot now — what a writer calls after its own write. */
    public void refresh() {
        snapshot = take(snapshot, clock.instant());
    }

    private Snapshot take(Snapshot last, Instant now) {
        try {
            return new Snapshot(Collections.unmodifiableMap(new HashMap<>(table.get())), now.plus(ttl));
        } catch (RuntimeException unreadable) {
            if (last != null) {
                LOG.log(System.Logger.Level.WARNING,
                        "the settings table could not be read - keeping the last snapshot for another TTL", unreadable);
                return new Snapshot(last.rows(), now.plus(ttl));
            }
            LOG.log(System.Logger.Level.WARNING,
                    "the settings table could not be read and there is no snapshot yet - the live level is vacant", unreadable);
            return new Snapshot(Map.of(), now.plus(ttl));
        }
    }
}

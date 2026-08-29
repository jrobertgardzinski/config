package com.jrobertgardzinski.config.ladder;

import com.jrobertgardzinski.config.domain.LiveConfigKey;
import com.jrobertgardzinski.config.domain.RebuildConfigKey;
import com.jrobertgardzinski.config.domain.RestartConfigKey;
import com.jrobertgardzinski.config.source.live.LiveConfigSource;
import com.jrobertgardzinski.config.source.rebuild.RebuildConfigSource;
import com.jrobertgardzinski.config.source.restart.RestartConfigSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * One logical configuration key resolved through a ladder of sources under a single, canonical
 * precedence law: the source bound LATEST in the lifecycle wins. A live value (a database row —
 * an administrator's decision now) beats a restart-bound one (a property or environment variable
 * — an operator's decision about this deployment), which beats the rebuild-bound default
 * (compile time — the programmer's fallback).
 *
 * <p>The order is deliberately not expressible in the API. A key declares WHAT it is — name,
 * default, mutability level — and the factory methods lay the rungs, so a mis-ordered ladder is
 * unrepresentable. The levels are named after what changing the value costs, and each level's
 * name is its own topmost rung:
 * <ul>
 *   <li>{@link #live live} — changing the value while the system runs has business value;
 *       rungs: live, restart, rebuild;</li>
 *   <li>{@link #restart restart} — changing the value is a deployment concern; rungs: restart,
 *       rebuild. Meta-configuration (the TTL of the cache in front of the live rung, say)
 *       belongs here, one level BELOW what it governs, so it can never delay its own
 *       correction.</li>
 * </ul>
 *
 * <p>The default is a mandatory argument validated eagerly, so {@link #resolve()} always has an
 * answer. Every candidate passes the same {@code gate} (typically a value-object constructor);
 * a rung whose value the gate rejects — a hand-edited database row, a typo in a property — is
 * logged and skipped, and the ladder falls through to the next rung. {@link #resolution()} tells
 * that story in full — the winning rung and every refusal on the way — for whoever has to
 * explain to an administrator why the value they wrote is not the value in force.
 *
 * <p>Resolution happens per {@link #resolve()} call and the ladder keeps no state: WHEN and HOW
 * OFTEN to ask is the caller's business. A use case asks per invocation and takes that snapshot
 * through the whole operation; wiring code may ask once at startup.
 */
public final class ConfigLadder<T> {

    private static final System.Logger LOG = System.getLogger(ConfigLadder.class.getName());

    public static final String LIVE_SOURCE = "live (database)";
    public static final String RESTART_SOURCE = "restart (properties/env)";
    public static final String REBUILD_SOURCE = "rebuild (default)";

    private record Rung<T>(String source, Supplier<Optional<T>> value) {
    }

    private final String name;
    private final T defaultValue;
    private final Consumer<T> gate;
    private final List<Rung<T>> rungs;
    private final RebuildConfigSource rebuild = new RebuildConfigSource();

    private ConfigLadder(String name, T defaultValue, Consumer<T> gate, List<Rung<T>> rungs) {
        gate.accept(defaultValue);   // the terminal rung must be legal, or there is no ladder at all
        this.name = name;
        this.defaultValue = defaultValue;
        this.gate = gate;
        this.rungs = rungs;
    }

    /** A key whose value may change while the system runs: live over restart over rebuild. */
    public static <T> ConfigLadder<T> live(String name, T defaultValue, Consumer<T> gate,
                                           LiveConfigSource<T> live,
                                           RestartConfigSource<T> restart) {
        return new ConfigLadder<>(name, defaultValue, gate, List.of(
                new Rung<>(LIVE_SOURCE, () -> live.resolve(new LiveConfigKey<>(name))),
                new Rung<>(RESTART_SOURCE, () -> restart.resolve(new RestartConfigKey<>(name)))));
    }

    /** A key whose change is a deployment concern: restart over rebuild, no live rung. */
    public static <T> ConfigLadder<T> restart(String name, T defaultValue, Consumer<T> gate,
                                              RestartConfigSource<T> restart) {
        return new ConfigLadder<>(name, defaultValue, gate, List.of(
                new Rung<>(RESTART_SOURCE, () -> restart.resolve(new RestartConfigKey<>(name)))));
    }

    public T resolve() {
        return resolution().value();
    }

    /** The answer together with its provenance: which rung answered, which rungs were refused and why. */
    public Resolution<T> resolution() {
        List<Resolution.Rejected<T>> rejected = new ArrayList<>();
        for (Rung<T> rung : rungs) {
            Optional<T> candidate = rung.value().get();
            if (candidate.isEmpty())
                continue;
            try {
                gate.accept(candidate.get());
                return new Resolution<>(candidate.get(), rung.source(), rejected);
            } catch (IllegalArgumentException illegal) {
                LOG.log(System.Logger.Level.WARNING,
                        "illegal value for ''{0}'' at the {1} rung ({2}) - falling through",
                        name, rung.source(), illegal.getMessage());
                rejected.add(new Resolution.Rejected<>(rung.source(), candidate.get(), illegal.getMessage()));
            }
        }
        return new Resolution<>(rebuild.resolve(new RebuildConfigKey<>(name, defaultValue)), REBUILD_SOURCE, rejected);
    }
}

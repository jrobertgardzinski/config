package com.jrobertgardzinski.config.ladder;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The one implementation of {@link ConfigLadder}: rungs in descending order, one per level, the
 * rebuild default at the bottom. Rungs bound before serving are read and gated once, here; a live
 * rung is read on every resolution.
 */
final class RungLadder<T> implements ConfigLadder<T> {

    private static final System.Logger LOG = System.getLogger(RungLadder.class.getName());

    private final String key;
    private final Consumer<T> gate;
    private final List<Rung<T>> rungs;
    /** What the rungs bound before serving held when the ladder was declared, already gated. */
    private final Map<Level, Optional<T>> boundBeforeServing = new EnumMap<>(Level.class);

    RungLadder(String key, Consumer<T> gate, List<Rung<T>> rungs) {
        this.key = Objects.requireNonNull(key, "key");
        this.gate = Objects.requireNonNull(gate, "gate");
        this.rungs = List.copyOf(rungs);
        requireWellFormed();
        for (Rung<T> rung : this.rungs) {
            if (rung.level().boundBeforeServing()) {
                boundBeforeServing.put(rung.level(), gatedNow(rung));
            }
        }
    }

    private void requireWellFormed() {
        if (rungs.isEmpty() || rungs.getLast().level() != Level.REBUILD) {
            throw new IllegalArgumentException(
                    "ladder for '" + key + "' must end in a rebuild default - there is no ladder without one");
        }
        for (int i = 1; i < rungs.size(); i++) {
            Level above = rungs.get(i - 1).level();
            Level below = rungs.get(i).level();
            if (above.compareTo(below) >= 0) {
                throw new IllegalArgumentException(
                        "ladder for '" + key + "' is out of order: " + above.label() + " may not sit above "
                                + below.label() + " - rungs go from the level bound latest to the default");
            }
        }
    }

    /** A rung bound before serving must be legal now, or there is no ladder at all. */
    private Optional<T> gatedNow(Rung<T> rung) {
        Optional<T> candidate = rung.read().apply(key);
        if (rung.level() == Level.REBUILD && candidate.isEmpty()) {
            throw new IllegalArgumentException("ladder for '" + key + "' has an empty rebuild default");
        }
        candidate.ifPresent(value -> {
            try {
                gate.accept(value);
            } catch (IllegalArgumentException illegal) {
                throw new IllegalArgumentException(
                        "illegal value for '" + key + "' at the " + rung.level().label() + " level (" + value + "): "
                                + illegal.getMessage(), illegal);
            }
        });
        return candidate;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public Resolution<T> resolution() {
        List<Resolution.Rejected<T>> rejected = new ArrayList<>();
        for (Rung<T> rung : rungs) {
            Optional<T> candidate = rung.level().boundBeforeServing()
                    ? boundBeforeServing.get(rung.level())
                    : rung.read().apply(key);
            if (candidate.isEmpty()) {
                continue;
            }
            T value = candidate.get();
            try {
                gate.accept(value);
                return new Resolution<>(value, rung.level().label(), rejected);
            } catch (IllegalArgumentException illegal) {
                // only a live rung can get here: the others were gated when the ladder was declared
                LOG.log(System.Logger.Level.WARNING,
                        "illegal value for ''{0}'' at the {1} level ({2}) - falling through",
                        key, rung.level().label(), illegal.getMessage());
                rejected.add(new Resolution.Rejected<>(rung.level().label(), value, illegal.getMessage()));
            }
        }
        throw new IllegalStateException("ladder for '" + key + "' ran out of rungs - the rebuild default is gated at declaration");
    }
}

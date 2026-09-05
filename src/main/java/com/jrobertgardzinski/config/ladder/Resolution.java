package com.jrobertgardzinski.config.ladder;

import java.util.List;

/**
 * What a {@link ConfigLadder} found on the way to its answer: the value, the label of the level
 * that supplied it, and every level above it that held a value the gate refused, with the gate's
 * own reason. An empty {@code rejected} list is a clean climb.
 */
public record Resolution<T>(T value, String source, List<Rejected<T>> rejected) {

    /** A level that held a value and was skipped: which level, what it held, and why it was refused. */
    public record Rejected<T>(String source, T value, String reason) {
    }

    public Resolution {
        rejected = List.copyOf(rejected);
    }
}

package com.jrobertgardzinski.config.ladder;

import java.util.List;

/**
 * What a {@link ConfigLadder} found on the way to its answer: the value, the rung that supplied
 * it, and every rung above that held a value the gate refused — in climbing order, each with the
 * gate's own reason. An empty {@code rejected} list is a clean climb. A caller that only wants
 * the value takes {@link #value()}; a caller that reports on configuration (an operator's
 * endpoint, a startup summary) has the whole story without re-resolving.
 */
public record Resolution<T>(T value, String source, List<Rejected<T>> rejected) {

    /** A rung that held a value and was skipped: which rung, what it held, and why it was refused. */
    public record Rejected<T>(String source, T value, String reason) {
    }

    public Resolution {
        rejected = List.copyOf(rejected);
    }
}

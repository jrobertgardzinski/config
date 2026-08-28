package com.jrobertgardzinski.config.domain;

/**
 * A key whose value lives in the source code — changing it costs a REBUILD of the artifact.
 * The value travels inside the key: it is always available, needs no external source, and is
 * the mandatory terminal of every ladder.
 */
public record RebuildConfigKey<T>(String name, T value) implements ConfigKey<T> {
}

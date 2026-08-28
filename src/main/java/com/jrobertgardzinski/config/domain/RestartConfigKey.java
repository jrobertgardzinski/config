package com.jrobertgardzinski.config.domain;

/**
 * A key bound when the process starts — application properties and environment variables alike
 * (the framework already arbitrates between those two) — so changing it costs a RESTART.
 */
public record RestartConfigKey<T>(String name) implements ConfigKey<T> {
}

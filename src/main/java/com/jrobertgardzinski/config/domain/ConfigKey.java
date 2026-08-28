package com.jrobertgardzinski.config.domain;

/**
 * One logical configuration key. The implementations are named after WHAT CHANGING THE VALUE
 * COSTS — the one fact a programmer needs at the call site — not after the technology behind it:
 * {@link RebuildConfigKey} (a new artifact), {@link RestartConfigKey} (a bounce of the process),
 * {@link LiveConfigKey} (nothing — the running system notices by itself).
 */
public sealed interface ConfigKey<T> permits RebuildConfigKey, RestartConfigKey, LiveConfigKey {
    String name();
}

package com.jrobertgardzinski.config.domain;

/**
 * A key whose value may change while the system runs — a database row an administrator edits —
 * and the running system notices by itself: changing it costs NOTHING beyond the staleness bound
 * of whatever cache sits in front (one TTL at most).
 */
public record LiveConfigKey<T>(String name) implements ConfigKey<T> {
}

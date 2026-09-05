package com.jrobertgardzinski.config;

/**
 * The contract of a configuration value object: it knows the name it goes by on every level of a
 * configuration ladder, the value it holds, and the value the code ships as the rebuild default.
 * The constructor is the gate - a value that is not legal never exists - so a ladder can be
 * declared from the type alone: {@code ConfigLadder.of(X.DEFAULT.key(), X::new, ..., Rung.rebuild(X.DEFAULT.defaultValue()))}.
 *
 * <p>Java cannot make an interface demand a static member, so the convention that every
 * implementation also exposes {@code public static final X DEFAULT} (and {@code String KEY}) is
 * held by a law test in each library, not by the compiler.
 */
public interface ConfigValue<T> {

    /** The name on every level: the property, the database row, the report. */
    String key();

    T value();

    /** What the code ships: the rebuild level, the bottom of every ladder. */
    T defaultValue();
}

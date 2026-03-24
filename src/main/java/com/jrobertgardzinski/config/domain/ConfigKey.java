package com.jrobertgardzinski.config.domain;

public sealed interface ConfigKey<T> permits HardcodedKey, PropertiesKey, RepositoryKey {
    String name();
}

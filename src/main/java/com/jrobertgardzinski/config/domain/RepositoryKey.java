package com.jrobertgardzinski.config.domain;

public record RepositoryKey<T>(String name) implements ConfigKey<T> {
}

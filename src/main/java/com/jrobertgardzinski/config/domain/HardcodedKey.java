package com.jrobertgardzinski.config.domain;

public record HardcodedKey<T>(String name, T value) implements ConfigKey<T> {
}

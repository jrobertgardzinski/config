package com.jrobertgardzinski.config.domain;

public record PropertiesKey<T>(String name) implements ConfigKey<T> {
}

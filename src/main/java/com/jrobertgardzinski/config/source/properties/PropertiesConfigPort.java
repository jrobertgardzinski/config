package com.jrobertgardzinski.config.source.properties;

public interface PropertiesConfigPort<T> {
    T get(String name);
}

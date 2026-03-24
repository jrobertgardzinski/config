package com.jrobertgardzinski.config.source.properties;

import com.jrobertgardzinski.config.domain.PropertiesKey;

public class PropertiesConfigSource<T> {

    private final PropertiesConfigPort<T> port;

    public PropertiesConfigSource(PropertiesConfigPort<T> port) {
        this.port = port;
    }

    public T resolve(PropertiesKey<T> key) {
        return port.get(key.name());
    }
}

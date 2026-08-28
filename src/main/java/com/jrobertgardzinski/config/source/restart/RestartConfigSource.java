package com.jrobertgardzinski.config.source.restart;

import com.jrobertgardzinski.config.domain.RestartConfigKey;

import java.util.Optional;

public class RestartConfigSource<T> {

    private final RestartConfigPort<T> port;

    public RestartConfigSource(RestartConfigPort<T> port) {
        this.port = port;
    }

    public Optional<T> resolve(RestartConfigKey<T> key) {
        return Optional.ofNullable(port.find(key.name()));
    }
}

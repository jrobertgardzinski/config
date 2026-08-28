package com.jrobertgardzinski.config.source.live;

import com.jrobertgardzinski.config.domain.LiveConfigKey;

import java.util.Optional;

public class LiveConfigSource<T> {

    private final LiveConfigPort<T> port;

    public LiveConfigSource(LiveConfigPort<T> port) {
        this.port = port;
    }

    public Optional<T> resolve(LiveConfigKey<T> key) {
        return Optional.ofNullable(port.find(key.name()));
    }
}

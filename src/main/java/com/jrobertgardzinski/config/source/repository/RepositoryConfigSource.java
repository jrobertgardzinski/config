package com.jrobertgardzinski.config.source.repository;

import com.jrobertgardzinski.config.domain.RepositoryKey;

import java.util.Optional;

public class RepositoryConfigSource<T> {

    private final RepositoryConfigPort<T> port;

    public RepositoryConfigSource(RepositoryConfigPort<T> port) {
        this.port = port;
    }

    public Optional<T> resolve(RepositoryKey<T> key) {
        return Optional.ofNullable(port.find(key.name()));
    }
}

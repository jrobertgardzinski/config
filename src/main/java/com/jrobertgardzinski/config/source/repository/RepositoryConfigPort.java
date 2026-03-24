package com.jrobertgardzinski.config.source.repository;

public interface RepositoryConfigPort<T> {
    T find(String name);
}

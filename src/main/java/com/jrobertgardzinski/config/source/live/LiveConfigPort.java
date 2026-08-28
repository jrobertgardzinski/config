package com.jrobertgardzinski.config.source.live;

/**
 * SPI over a store the running system re-reads on its own — a database table an administrator
 * edits. The name promises the SEMANTICS — a change is noticed without a restart — and the
 * adapter names the technology. Returns {@code null} when no value is set: absence is a vacant
 * rung for a layered resolver to fall through, not an error.
 */
public interface LiveConfigPort<T> {
    T find(String name);
}

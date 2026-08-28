package com.jrobertgardzinski.config.source.rebuild;

import com.jrobertgardzinski.config.domain.RebuildConfigKey;

/**
 * The source with no port: there is no technology to adapt, because the "technology" is the
 * source code itself and the compiler already guarantees the value is present. Kept as a class
 * so all three sources tell one uniform story.
 */
public class RebuildConfigSource {

    public <T> T resolve(RebuildConfigKey<T> key) {
        return key.value();
    }
}

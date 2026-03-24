package com.jrobertgardzinski.config.source.hardcoded;

import com.jrobertgardzinski.config.domain.HardcodedKey;

public class HardcodedConfigSource {

    public <T> T resolve(HardcodedKey<T> key) {
        return key.value();
    }
}

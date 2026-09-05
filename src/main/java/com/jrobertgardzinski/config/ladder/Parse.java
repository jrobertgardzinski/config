package com.jrobertgardzinski.config.ladder;

import java.util.Locale;

/**
 * Parsers for the text a property source or a settings table holds. Each refuses text that is not
 * its type with an {@link IllegalArgumentException}, which a rung turns into a refusal of that
 * level: at declaration for a level bound before serving, per resolution for the live one.
 */
public final class Parse {

    private Parse() {
    }

    public static Integer integer(String text) {
        return Integer.valueOf(text.trim());
    }

    /** Strict: {@code true} or {@code false}, any case, and nothing else. */
    public static Boolean bool(String text) {
        return switch (text.trim().toLowerCase(Locale.ROOT)) {
            case "true" -> Boolean.TRUE;
            case "false" -> Boolean.FALSE;
            default -> throw new IllegalArgumentException("must be true or false, not '" + text + "'");
        };
    }

    public static String text(String text) {
        return text;
    }
}

package com.jrobertgardzinski.config;

import com.jrobertgardzinski.config.ladder.ConfigLadder;
import com.jrobertgardzinski.config.ladder.Parse;
import com.jrobertgardzinski.config.ladder.Rung;
import com.jrobertgardzinski.config.source.live.LiveConfigPort;
import com.jrobertgardzinski.config.source.restart.RestartConfigPort;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A deployment's configuration: the two sources a running system has, and the two ways a rule
 * can be read from them, each named after what changing the value costs. Both start from the
 * rule the code ships - the rebuild default every ladder ends in - and take the key, the type's
 * parser and the gate (the constructor) from it: {@code liveOver(MinLength.DEFAULT)}.
 *
 * <ul>
 *   <li>{@link #liveOver}: live over restart over rebuild, read on every resolution - for a rule
 *       the system asks about per use (a policy).</li>
 *   <li>{@link #boundOver}: restart over rebuild, resolved here and now - for a rule the system
 *       holds as a value for its whole life; an illegal property refuses at declaration.</li>
 * </ul>
 */
public final class Configuration {

    private final LiveConfigPort<String> rows;
    private final RestartConfigPort<String> properties;

    public Configuration(LiveConfigPort<String> rows, RestartConfigPort<String> properties) {
        this.rows = rows;
        this.properties = properties;
    }

    /** A ladder over the three levels, answering with the rule holding the value in force. */
    public <T, V extends ConfigValue<T>> ConfigLadder<V> liveOver(V shipped) {
        return ladder(shipped, Rung.live(rows, parserOf(shipped)));
    }

    /** The rule as the deployment binds it: its property over the shipped default, decided now. */
    public <T, V extends ConfigValue<T>> V boundOver(V shipped) {
        return ladder(shipped).resolve();
    }

    /**
     * Every ladder ends the same way - the property over the shipped default - and answers in
     * the rule's own type; what sits above is the caller's choice. The gate is the rule's
     * constructor, through {@code holding}: a candidate it refuses is reported with the value
     * it held, so the refusal stays a number in the report, not the text it was parsed from.
     */
    @SafeVarargs
    private <T, V extends ConfigValue<T>> ConfigLadder<V> ladder(V shipped, Rung<T>... above) {
        List<Rung<T>> rungs = new ArrayList<>(List.of(above));
        rungs.add(Rung.restart(properties, parserOf(shipped)));
        rungs.add(Rung.rebuild(shipped.defaultValue()));
        return ConfigLadder.of(shipped.key(), value -> shipped.holding(value), rungs)
                .map(value -> holding(shipped, value));
    }

    private static <T, V extends ConfigValue<T>> Function<String, T> parserOf(V shipped) {
        return Parse.forType(shipped.defaultValue().getClass());
    }

    /** {@code holding} answers with the rule's own type - a law in every library holds that promise. */
    @SuppressWarnings("unchecked")
    private static <T, V extends ConfigValue<T>> V holding(V shipped, T value) {
        return (V) shipped.holding(value);
    }
}

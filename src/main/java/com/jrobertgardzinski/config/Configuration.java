package com.jrobertgardzinski.config;

import com.jrobertgardzinski.config.ladder.ConfigLadder;
import com.jrobertgardzinski.config.ladder.Parse;
import com.jrobertgardzinski.config.ladder.Rung;
import com.jrobertgardzinski.config.source.live.LiveConfigPort;
import com.jrobertgardzinski.config.source.restart.RestartConfigPort;

import java.util.function.Consumer;
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
        Function<String, T> parser = Parse.forType(shipped.defaultValue().getClass());
        return ConfigLadder.of(shipped.key(), gateOf(shipped),
                        Rung.live(rows, parser), Rung.restart(properties, parser), Rung.rebuild(shipped.defaultValue()))
                .map(value -> holding(shipped, value));
    }

    /** The rule as the deployment binds it: its property over the shipped default, decided now. */
    public <T, V extends ConfigValue<T>> V boundOver(V shipped) {
        Function<String, T> parser = Parse.forType(shipped.defaultValue().getClass());
        T value = ConfigLadder.of(shipped.key(), gateOf(shipped),
                Rung.restart(properties, parser), Rung.rebuild(shipped.defaultValue())).resolve();
        return holding(shipped, value);
    }

    private static <T, V extends ConfigValue<T>> Consumer<T> gateOf(V shipped) {
        return value -> shipped.holding(value);
    }

    /** {@code holding} answers with the rule's own type - a law in every library holds that promise. */
    @SuppressWarnings("unchecked")
    private static <T, V extends ConfigValue<T>> V holding(V shipped, T value) {
        return (V) shipped.holding(value);
    }
}

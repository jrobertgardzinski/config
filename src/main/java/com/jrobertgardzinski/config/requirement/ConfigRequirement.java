package com.jrobertgardzinski.config.requirement;

import com.jrobertgardzinski.config.domain.RestartConfigKey;
import com.jrobertgardzinski.config.source.restart.RestartConfigSource;

import java.util.function.Consumer;

/**
 * A configuration key the system REFUSES TO START without — the ladder's opposite. A ladder's
 * essence is fallback (a mandatory default guarantees an answer); a requirement's essence is the
 * refusal to fall back: no default exists, because for this kind of key a default is a lie —
 * a real secret placed in the repository, or a "working" value that masks a misconfigured
 * deployment by quietly pointing somewhere that answers.
 *
 * <p>Only the RESTART source (properties, environment variables) can carry a requirement, and
 * the exclusions are made unrepresentable the same way the ladder's rung order is — there is no
 * factory for them. The rebuild source cannot: a value whose only source is the code is a
 * constant, and the compiler already enforces constants. The live source must not: a boot that
 * depends on a database row nobody can insert yet (a fresh environment) or anyone can delete
 * (a fleet that quietly becomes unrestartable, discovered at the NEXT restart) is a boot-order
 * trap. If a value must be both mandatory and runtime-changeable, require it at restart level
 * and put a live rung ABOVE it in a ladder.
 *
 * <p>Resolve EAGERLY, in the composition root, at startup — the point is to fail before traffic,
 * with a message that tells the operator exactly what to set. The same gate the ladder uses
 * applies here; an illegal value refuses the start just like an absent one, because there is
 * nothing to fall through to.
 */
public final class ConfigRequirement<T> {

    private final String name;
    private final Consumer<T> gate;
    private final RestartConfigSource<T> source;

    private ConfigRequirement(String name, Consumer<T> gate, RestartConfigSource<T> source) {
        this.name = name;
        this.gate = gate;
        this.source = source;
    }

    /** A key that MUST arrive with the deployment: a property or environment variable, no default. */
    public static <T> ConfigRequirement<T> restart(String name, Consumer<T> gate,
                                                   RestartConfigSource<T> source) {
        return new ConfigRequirement<>(name, gate, source);
    }

    public T resolve() {
        T value = source.resolve(new RestartConfigKey<>(name)).orElseThrow(() -> new IllegalStateException(
                "required configuration '" + name + "' is not set - provide it as a property"
                        + " or the matching environment variable; there is deliberately no default"));
        try {
            gate.accept(value);
        } catch (IllegalArgumentException illegal) {
            throw new IllegalStateException(
                    "required configuration '" + name + "' holds an illegal value ("
                            + illegal.getMessage() + ") - there is nothing to fall back to", illegal);
        }
        return value;
    }
}

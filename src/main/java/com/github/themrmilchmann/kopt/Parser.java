package com.github.themrmilchmann.kopt;

import javax.annotation.Nullable;

/**
 * A {@code Parser} provides a conversion method to convert a
 * {@linkplain String} to a value.
 *
 * <p>This class contains predefined parsers for frequently used primitives.</p>
 *
 * @since 1.0.0
 *
 * @author Leon Linhart <themrmilchmann@gmail.com>
 */
@FunctionalInterface
public interface Parser<VT> {

    /**
     * A simple parser for parsing {@code Boolean} values.
     *
     * <p>This parser delegates to {@link Boolean#valueOf(String)}</p>
     *
     * @since 1.0.0
     */
    Parser<Boolean> BOOLEAN = (it) -> it.equals("1") || it.equals("true");

    /**
     * A simple parser for parsing {@code Byte} values.
     *
     * <p>This parser delegates to {@link Byte#valueOf(String)}</p>
     *
     * @since 1.0.0
     */
    Parser<Byte> BYTE = Byte::valueOf;

    /**
     * A simple parser for parsing {@code Short} values.
     *
     * <p>This parser delegates to {@link Short#valueOf(String)}</p>
     *
     * @since 1.0.0
     */
    Parser<Short> SHORT = Short::valueOf;

    /**
     * A simple parser for parsing {@code Integer} values.
     *
     * <p>This parser delegates to {@link Integer#valueOf(String)}</p>
     *
     * @since 1.0.0
     */
    Parser<Integer> INT = Integer::valueOf;

    /**
     * A simple parser for parsing {@code Long} values.
     *
     * <p>This parser delegates to {@link Long#valueOf(String)}</p>
     *
     * @since 1.0.0
     */
    Parser<Long> LONG = Long::valueOf;

    /**
     * A simple parser for parsing {@code Float} values.
     *
     * <p>This parser delegates to {@link Float#valueOf(String)}</p>
     *
     * @since 1.0.0
     */
    Parser<Float> FLOAT = Float::valueOf;

    /**
     * A simple parser for parsing {@code Double} values.
     *
     * <p>This parser delegates to {@link Double#valueOf(String)}</p>
     *
     * @since 1.0.0
     */
    Parser<Double> DOUBLE = Double::valueOf;

    /**
     * Parse a value from a given {@linkplain String}.
     *
     * @param string the {@code String} to be parsed
     *
     * @return the parsed value, or {@code null}
     *
     * @throws ParsingException if an error occurs while parsing
     *
     * @since 1.0.0
     */
    @Nullable
    VT parse(String string) throws ParsingException;

}
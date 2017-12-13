/*
 * Copyright (c) 2017 Leon Linhart,
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
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
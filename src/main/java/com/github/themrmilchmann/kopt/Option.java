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
 * A command line option.
 *
 * The syntax that may be used for an option depends on which attributes of the
 * option have been assigned.
 *
 * <ul>
 *     <li><pre>{@code --<longToken>(=<value>| <value)}
 *       the default syntax</pre></li>
 *     <li><pre>{@code --<longToken>[(=<value>| <value>)]}
 *       syntax for options with a marker value</pre></li>
 *     <li><pre>{@code --<longToken>}
 *       syntax for marker only options </pre></li>
 * </ul>
 *
 * If the option has a `shortToken`, it may also be used:
 *
 * <ul>
 *     <li><pre>{@code -<shortToken>(=<value>| <value)}
 *       the default syntax</pre></li>
 *     <li><pre>{@code -<shortToken>[(=<value>| <value>)]}
 *       syntax for options with a marker value</pre></li>
 *     <li><pre>{@code -<shortToken>}
 *       syntax for marker only options </pre></li>
 * </ul>
 *
 * Using the short token of an option may be useful when setting the same value
 * for multiple options. (`-abc` is equivalent to `-a -b -c` and `-abc="d"` is
 * equivalent to `-a="d" -b="d" -c="d"`.)
 *
 * @param <VT> the type of the arguments value
 *
 * @see OptionParser
 * @see Argument
 *
 * @since 1.0.0
 *
 * @author Leon Linhart <themrmilchmann@gmail.com>
 */
public final class Option<VT> {

    @Nullable
    private final Character shortToken;
    private final String longToken;

    private final Parser<VT> parser;

    @Nullable
    private final Validator<VT> validator;

    @Nullable
    private final VT defaultValue;
    private final boolean hasDefaultValue;

    @Nullable
    private final VT markerValue;
    private final boolean hasMarkerValue;
    private final boolean isMarkerOnly;

    private Option(String longToken, @Nullable Character shortToken, Parser<VT> parser, @Nullable Validator<VT> validator, @Nullable VT defaultValue, boolean hasDefaultValue, @Nullable VT markerValue, boolean hasMarkerValue, boolean isMarkerOnly) {
        this.shortToken = shortToken;
        this.longToken = longToken;
        this.parser = parser;
        this.validator = validator;
        this.defaultValue = defaultValue;
        this.hasDefaultValue = hasDefaultValue;
        this.markerValue = markerValue;
        this.hasMarkerValue = hasMarkerValue;
        this.isMarkerOnly = isMarkerOnly;
    }

    /**
     * Returns this option's long token.
     *
     * @return this option's long token
     *
     * @since 1.0.0
     */
    public String getLongToken() {
        return this.longToken;
    }

    /**
     * Returns this option's short token, or {@code null} if this option has no
     * short token.
     *
     * @return this option's short token, or {@code null}
     *
     * @see 1.0.0
     */
    @Nullable
    public Character getShortToken() {
        return this.shortToken;
    }

    /**
     * Returns this option's default value.
     *
     * @return this option's default value
     *
     * @throws IllegalArgumentException if this option does not have a default value
     *
     * @see #hasDefaultValue()
     *
     * @since 1.0.0
     */
    @Nullable
    public VT getDefaultValue() {
        if (!this.hasDefaultValue) throw new IllegalStateException(this.toString() + " does not have a default value");
        return this.defaultValue;
    }

    /**
     * Returns whether or not this option has a default value.
     *
     * @return {@code true} if this option has a default value, or {@code false}
     *         otherwise
     *
     * @since 1.0.0
     */
    public boolean hasDefaultValue() {
        return this.hasDefaultValue;
    }

    /**
     * Returns this option's marker value.
     *
     * @return  this option's marker value.
     *
     * @since 1.0.0
     */
    @Nullable
    public VT getMarkerValue() {
        if (!this.hasMarkerValue) throw new IllegalStateException(this.toString() + " does not have a marker value");
        return this.markerValue;
    }

    /**
     * Returns whether or not this option has a marker value.
     *
     * @return {@code true} if this option has a marker value, or {@code false}
     *         otherwise
     *
     * @since 1.0.0
     */
    public boolean hasMarkerValue() {
        return this.hasMarkerValue;
    }

    /**
     * Returns whether or not this option must be used as a marker option.
     *
     * @return {@code true} if this option must be used as a marker, or
     *         {@code false} otherwise
     *
     * @since 1.0.0
     */
    public boolean isMarkerOnly() {
        return this.isMarkerOnly;
    }

    @Nullable
    VT parse(String string) throws ParsingException {
        return this.parser.parse(string);
    }

    void validate(@Nullable VT value) throws ValidationException {
        if (this.validator != null) this.validator.validate(value);
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0.0
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Option[");
        sb.append("long=").append(this.longToken);
        if (this.shortToken != null) sb.append(", short=").append(this.shortToken);
        sb.append(", hasDefaultValue=").append(this.hasDefaultValue);
        if (this.hasDefaultValue) sb.append(", defaultValue=").append(this.defaultValue);
        sb.append(", hasMarkerValue=").append(this.hasMarkerValue);
        if (this.hasMarkerValue) sb.append(", markerValue=").append(this.markerValue);
        sb.append(", markerOnly=").append(this.isMarkerOnly);
        sb.append("]");

        return sb.toString();
    }

    /**
     * A builder for an {@link Option}.
     *
     * @param <VT> the type for the option's value
     *
     * @since 1.0.0
     */
    public static final class Builder<VT> {

        private final String longToken;
        private final Parser<VT> parser;

        @Nullable
        private Character shortToken;

        @Nullable
        private Validator<VT> validator;

        @Nullable
        private VT defaultValue;
        private boolean hasDefaultValue;

        @Nullable
        private VT markerValue;
        private boolean hasMarkerValue;
        private boolean isMarkerOnly;

        public Builder(String longToken, Parser<VT> parser) {
            this.longToken = longToken;
            this.parser = parser;
        }

        /**
         * Returns a new immutable {@linkplain Option}.
         *
         * @return a new immutable argument
         *
         * @since 1.0.0
         */
        public Option<VT> create() {
            return new Option<>(this.longToken, this.shortToken, this.parser, this.validator, this.defaultValue, this.hasDefaultValue, this.markerValue, this.hasMarkerValue, this.isMarkerOnly);
        }

        /**
         * Sets the default value for the option.
         *
         * <p>Overrides any previously set default value.</p>
         *
         * @param value the default value for the option
         *
         * @return this builder instance
         *
         * @since 1.0.0
         */
        public Builder<VT> withDefaultValue(@Nullable VT value) {
            this.defaultValue = value;
            this.hasDefaultValue = true;

            return this;
        }

        /**
         * Sets the marker value for the option.
         *
         * @param value         the marker value for the option
         * @param isMarkerOnly  whether or not the option must be used as a
         *                      marker
         *
         * @return this builder instance
         *
         * @since 1.0.0
         */
        public Builder<VT> withMarkerValue(@Nullable VT value, boolean isMarkerOnly) {
            this.markerValue = value;
            this.hasMarkerValue = true;
            this.isMarkerOnly = isMarkerOnly;

            return this;
        }

        /**
         * Sets the short token for the option.
         *
         * @param shortToken the short token for the option
         *
         * @return this builder instance
         *
         * @since 1.0.0
         */
        public Builder<VT> withShortToken(char shortToken) {
            this.shortToken = shortToken;

            return this;
        }

        /**
         * Sets the validator for the option.
         *
         * <p>Overrides any previously set validator.</p>
         *
         * @param validator the validator for the option
         *
         * @return this builder instance
         *
         * @since 1.0.0
         */
        public Builder<VT> withValidator(Validator<VT> validator) {
            this.validator = validator;

            return this;
        }

    }

}
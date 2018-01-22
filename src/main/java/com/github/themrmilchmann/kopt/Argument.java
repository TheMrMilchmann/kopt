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
 * A command line argument.
 *
 * @param <VT> the type of the arguments value
 *
 * @since 1.0.0
 *
 * @see OptionParser
 * @see Option
 *
 * @author Leon Linhart <themrmilchmann@gmail.com>
 */
public final class Argument<VT> {

    private final Parser<VT> parser;
    private final boolean isOptional;

    @Nullable
    private final Validator<VT> validator;

    @Nullable
    private final VT defaultValue;
    private final boolean hasDefault;

    private Argument(Parser<VT> parser, boolean isOptional, @Nullable Validator<VT> validator, @Nullable VT defaultValue, boolean hasDefault) {
        this.parser = parser;
        this.isOptional = isOptional;
        this.validator = validator;
        this.defaultValue = defaultValue;
        this.hasDefault = hasDefault;
    }

    /**
     * Returns the default value for this argument.
     *
     * @return the default value for this argument
     *
     * @throws IllegalStateException if this argument does not have a default
     *                               value
     *
     * @since 1.0.0
     */
    @Nullable
    public VT getDefaultValue() {
        if (!this.hasDefault) throw new IllegalStateException("Argument does not have a default value: " + this.toString());
        return this.defaultValue;
    }

    /**
     * Returns whether or not this argument has a default value.
     *
     * @return {@code true} if this argument has a default value, or
     *         {@code false} otherwise
     *
     * @since 1.0.0
     */
    public boolean hasDefault() {
        return this.hasDefault;
    }

    /**
     * Returns whether or not this argument is optional.
     *
     * @return {@code true} if this argument is optional, or {@code false}
     *         otherwise
     *
     * @since 1.0.0
     */
    public boolean isOptional() {
        return this.isOptional;
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
        StringBuilder sb = new StringBuilder("Argument[");
        sb.append("isOptional=").append(this.isOptional);
        sb.append(", hasDefault=").append(this.hasDefault);
        if (this.hasDefault) sb.append(", defaultValue=").append(this.defaultValue);
        sb.append("]");

        return sb.toString();
    }

    /**
     * A builder for an {@link Argument}.
     *
     * @param <VT> the type for the arguments value
     *
     * @since 1.0.0
     */
    public static final class Builder<VT> {

        private final Parser<VT> parser;
        private final boolean isOptional;

        @Nullable
        private Validator<VT> validator;

        @Nullable
        private VT defaultValue;
        private boolean hasDefault;

        public Builder(Parser<VT> parser) {
            this(parser, false);
        }

        public Builder(Parser<VT> parser, boolean isOptional) {
            this.parser = parser;
            this.isOptional = isOptional;
        }

        /**
         * Returns a new immutable {@linkplain Argument}.
         *
         * @return a new immutable argument
         *
         * @since 1.0.0
         */
        public Argument<VT> create() {
            return new Argument<>(this.parser, this.isOptional, this.validator, this.defaultValue, this.hasDefault);
        }

        /**
         * Sets the default value for the argument.
         *
         * <p>Overrides any previously set default value.</p>
         *
         * @param value the default value for the argument
         *
         * @return this builder instance
         *
         * @since 1.0.0
         */
        public Builder<VT> withDefaultValue(@Nullable VT value) {
            this.defaultValue = value;
            this.hasDefault = true;

            return this;
        }

        /**
         * Sets the validator for the argument.
         *
         * <p>Overrides any previously set validator.</p>
         *
         * @param validator the validator for the argument
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
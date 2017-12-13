package com.github.themrmilchmann.kopt;

import javax.annotation.Nullable;

/**
 * A command line option.
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
    private final boolean hasDefault;
    private final boolean requiresValue;

    private Option(String longToken, @Nullable Character shortToken, Parser<VT> parser, @Nullable Validator<VT> validator, @Nullable VT defaultValue, boolean hasDefault, boolean requiresValue) {
        this.shortToken = shortToken;
        this.longToken = longToken;
        this.parser = parser;
        this.validator = validator;
        this.defaultValue = defaultValue;
        this.hasDefault = hasDefault;
        this.requiresValue = requiresValue;
    }

    /**
     * Returns this option's default value.
     *
     * @return this option's default value
     *
     * @throws IllegalArgumentException if this option does not have a default value
     *
     * @see #hasDefault()
     *
     * @since 1.0.0
     */
    @Nullable
    public VT getDefaultValue() {
        if (!this.hasDefault) throw new IllegalStateException(this.toString() + " does not have a default value");
        return this.defaultValue;
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
     * Returns whether or not this option has a default value-
     *
     * @return `true` of this option has a default value, or `false` otherwise
     *
     * @since 1.0.0
     */
    public boolean hasDefault() {
        return this.hasDefault;
    }

    /**
     * Returns whether or not this option requires a value to be specified
     * explicitly.
     *
     * @return {@code true} if this option requires a value, or {@code false}
     *         otherwise
     *
     * @since 1.0.0
     */
    public boolean isValueRequired() {
        return this.requiresValue;
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
        sb.append(", requiresValue=").append(this.requiresValue);
        sb.append(", hasDefault=").append(this.hasDefault);
        if (this.hasDefault) sb.append(", defaultValue=").append(this.defaultValue);
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
        private boolean hasDefault;
        private boolean requiresValue;

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
            return new Option<>(this.longToken, this.shortToken, this.parser, this.validator, this.defaultValue, this.hasDefault, this.requiresValue);
        }

        /**
         * Sets the default value for the option.
         *
         * <p>Overrides any previously set default value.</p>
         *
         * @param value         the default value for the option
         * @param requiresValue whether or not this option requires a value to
         *                      be passed
         *
         * @return this builder instance
         *
         * @since 1.0.0
         */
        public Builder<VT> withDefaultValue(@Nullable VT value, boolean requiresValue) {
            this.defaultValue = value;
            this.hasDefault = true;
            this.requiresValue = requiresValue;

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
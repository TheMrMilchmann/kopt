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
        public Builder<VT> withValidator(@Nullable Validator<VT> validator) {
            this.validator = validator;

            return this;
        }

    }

}
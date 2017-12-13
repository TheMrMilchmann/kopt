package com.github.themrmilchmann.kopt;

import javax.annotation.Nullable;

/**
 * A validator provides a way to validate a value.
 *
 * <p>Validators may be attached to {@linkplain Argument}s and
 * {@linkplain Option}s. (Take a look at the respective constructor for more
 * information.)</p>
 *
 * @param <VT> the type of the values to be validated
 *
 * @see OptionParser
 * @see Option
 * @see Argument
 *
 * @since 1.0.0
 *
 * @author Leon Linhart <themrmilchmann@gmail.com>
 */
@FunctionalInterface
public interface Validator<VT> {

    /**
     * Validates the given value by throwing on error.
     *
     * <p><b>WARNING:</b> This method may throw an arbitrary error and should be
     * used carefully.</p>
     *
     * @since 1.0.0
     */
    void validate(@Nullable VT value) throws ValidationException;

}
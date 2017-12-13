package com.github.themrmilchmann.kopt

/**
 * A `ValidationException` may be thrown to indicate failure when validating a
 * value.
 *
 * A `ValidationException` may either be thrown explicitly, with an given error
 * message, or it will be thrown implicitly with additional details about the
 * cause.
 *
 * @since 1.0.0
 *
 * @author Leon Linhart <themrmilchmann@gmail.com>
 */
class ValidationException(msg: String, override val cause: Exception? = null) : Exception(msg)
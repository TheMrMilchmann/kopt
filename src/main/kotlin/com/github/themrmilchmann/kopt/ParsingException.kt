package com.github.themrmilchmann.kopt

/**
 * A `ParsingException` indicates failure while parsing a stream or sequence.
 * The message of such an exception should never be empty and does often provide
 * information that is crucial to determine the exact cause of the crash.
 *
 * @param [msg] the cause of the crash
 *
 * @since 1.0.0
 *
 * @author Leon Linhart <themrmilchmann@gmail.com>
 */
class ParsingException(msg: String) : Exception(msg)
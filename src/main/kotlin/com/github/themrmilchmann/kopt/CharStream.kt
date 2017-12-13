@file:JvmName("CharStreams")
package com.github.themrmilchmann.kopt

import kotlin.jvm.*

/**
 * Returns a new stream representation for a given sequence.
 *
 * @receiver the sequence to wrap
 *
 * @return a stream representation of the given sequence
 *
 * @since 1.0.0
 */
val CharSequence.stream
    @JvmName("streamOf") get() = object : CharStream() {

        override fun read(): Char? =
            (if (position < length - 1) get(++position) else null)

    }

/**
 * A parsable stream of characters.
 *
 * @see OptionParser
 *
 * @since 1.0.0
 *
 * @author Leon Linhart <themrmilchmann@gmail.com>
 */
abstract class CharStream {

    /**
     * The current position.
     *
     * The initial position is `-1`, and the maximal position is the upper limit
     * of the source.
     *
     * @since 1.0.0
     */
    var position: Int = -1
        protected set

    /**
     * The character at the current position or `null` if the position is not
     * within the source's boundaries.
     *
     * @since 1.0.0
     */
    var current: Char? = null
        protected set

    /**
     * Returns the next character or `null` if the end of the stream has been
     * reached.
     *
     * @return the next character or `null` if the end of the stream has been
     *         reached
     *
     * @since 1.0.0
     */
    fun next(): Char? = read().also { current = it }

    /**
     * Returns the next non whitespace character or `null`.
     *
     * @return the next non whitespace character or `null`
     *
     * @since 1.0.0
     */
    fun nextNonWhitespace(): Char? {
        while (next() !== null && current!!.isWhitespace());
        return current
    }

    protected abstract fun read(): Char?

    /**
     * Parses a literal starting from the current character.
     *
     * @return the parsed string or `null`
     *
     * @since 1.0.0
     */
    fun currentLiteral(): String? =
        if (position == -1 || current === null)
            null
        else
            StringBuilder().apply {
                append(current!!)

                while (next() !== null && current != ' ') {
                    append(current!!)
                }
            }.toString()

    /**
     * Parses a literal starting from the next character.
     *
     * @return the parsed string or `null`
     *
     * @since 1.0.0
     */
    fun nextLiteral(): String? = StringBuilder().apply {
        while (next() !== null && current != ' ') append(current)
    }.toString()

    /**
     * Parses a `String` starting from the current character.
     *
     * @return the parsed string or `null`
     *
     * @since 1.0.0
     */
    fun currentString(): String? =
        if (position == -1 || current === null)
            null
        else
            StringBuilder().apply {
                when (current) {
                    '"' ->  {
                        var escapeNext = false

                        while (next() !== null && !(current == '"' && !escapeNext)) {
                            if (current == '\\' && !escapeNext)
                                escapeNext = true
                            else {
                                append(current!!)
                                escapeNext = false
                            }
                        }

                        if (current != '"') return null
                    }
                    else -> {
                        append(current!!)

                        while (next() !== null && current != ' ') {
                            append(current!!)
                        }
                    }
                }
            }.toString()

    /**
     * Parses a `String` starting from the next character.
     *
     * @return the parsed string or `null`
     *
     * @since 1.0.0
     */
    fun nextString(): String? = when (next()) {
        null -> null
        '"' -> currentString()
        else -> currentLiteral()
    }

}
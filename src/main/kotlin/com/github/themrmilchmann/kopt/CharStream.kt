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
 * Joins all elements of the array to a single String and returns a new stream
 * for the joined String.
 *
 * @receiver the array to wrap
 *
 * @return a stream representation of the given aray
 *
 * @since 1.0.0
 */
val Array<String>.stream
    @JvmName("streamOf") get() = this.joinToString(" ") {
        "\"${it.removePrefix("\"")
            .removeSuffix("\"")
            .replace("\"", "\\\"")}\""
    }.stream

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
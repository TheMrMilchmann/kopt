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
@file:JvmName("CharTools")
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

        var position: Int = -1

        override fun available(): Int =
            if (position == - 1) length else length - position - 1

        override fun read(): Char? =
            (if (position < length - 1) get(++position) else null)

    }

val Char.isAlphabetic: Boolean get() = this in 'A'..'Z' || this in 'a'..'z'
val Char.isNumeric: Boolean get() = this in '0'..'9'
val Char.isAlphanumeric: Boolean get() = isAlphabetic || isNumeric

/**
 * Joins all elements of the array to a single String and returns a new stream
 * for the joined String.
 *
 * @receiver the array to wrap
 *
 * @return a stream representation of the given array
 *
 * @since 1.0.0
 */
val Array<String>.stream
    @JvmName("streamOf") get() = this.joinToString(" ") {
        if (it.startsWith('-'))
            it
        else
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
     * The last character that has been read.
     *
     * @since 1.0.0
     */
    var last: Char? = null
        private set

    abstract fun available(): Int

    protected abstract fun read(): Char?

    fun next(): Char? = read().also { last = it }

    fun skip(advance: Int): Char? {
        if (last === null && available() > 0) next()
        for (i in 0 until advance) {
            if (next() === null) return null
        }
        return last
    }

    fun skip(advance: Int = 0, until: (Char) -> Boolean): Char? {
        if (skip(advance) === null) return null
        while (available() > 0  && !until.invoke(last!!)) next()
        return last
    }

    private fun collect(until: (Char) -> Boolean, onEach: ((Char) -> Unit)? = null): String? =
        if (available() < 1 && last === null)
            null
        else
            StringBuilder().run {
                if (last === null && available() > 0) next()

                while (last !== null && !until.invoke(last!!)) {
                    onEach?.invoke(last!!)
                    append(last!!)
                    next()
                }

                toString()
            }

    fun readShortOptionTokenChainOrArg(advance: Int = 0): String? = when (skip(advance)) {
        null -> null
        else -> collect({ it.isWhitespace() || it == '=' })
    }

    fun readLongOptionToken(advance: Int = 0): String? = when (skip(advance)) {
        null -> null
        else -> collect({ it.isWhitespace() || it == '=' }) { if (!it.isAlphanumeric) throw ParsingException("Illegal character: $it") }
    }

    fun readString(advance: Int = 0, skipWhitespace: Boolean = false): String? {
        skip(advance)
        if (skipWhitespace) skip(until = { !it.isWhitespace() })
        return when (last) {
            null    -> null
            '"'     -> StringBuilder().apply {
                when (last) {
                    '"' -> {
                        var escapeNext = false

                        while (next() !== null && !(last == '"' && !escapeNext)) {
                            escapeNext = if (last == '\\' && !escapeNext)
                                true
                            else {
                                append(last!!)
                                false
                            }
                        }

                        /* Consume the trailing quotation mark. */
                        next()
                    }
                    else -> {
                        append(last!!)

                        while (next() !== null && !last!!.isWhitespace()) {
                            append(last!!)
                        }
                    }
                }
            }.toString()
            else    -> collect(Char::isWhitespace)
        }
    }

}
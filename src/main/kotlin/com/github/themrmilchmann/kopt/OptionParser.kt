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
@file:JvmName("OptionParser")
@file:Suppress("NOTHING_TO_INLINE")
package com.github.themrmilchmann.kopt

import kotlin.jvm.*

private val Char.isAlphabetic: Boolean get() = (this in 'A'..'Z' || this in 'a'..'z')
private val Char.isNumeric: Boolean get() = (this in '0'..'9')
private val Char.isAlphanumeric: Boolean get() = isAlphabetic || isNumeric

/**
 * Parses a `CharStream` for values taken into account all options available in
 * the given pool.
 *
 * @receiver the stream to be parsed
 *
 * @param pool the pool of available options
 *
 * @return an immutable set of values parsed from this stream
 *
 * @throws ParsingException     if an error occurred while parsing
 * @throws ValidationException  if a parsed value was invalid
 *
 * @since 1.0.0
 */
fun CharStream.parse(pool: OptionPool): OptionSet {
    val values = mutableMapOf<Any, Any?>()
    val _varargValues = lazy { mutableListOf<Any?>() }
    val varargValues by _varargValues
    var argIndex = 0
    var ignoreOptions = false

    fun currentNonWhitespace() =
        if (current !== null && !current!!.isWhitespace()) current else nextNonWhitespace()

    fun appendArgument(value: String) {
        val arg = pool.args[argIndex]
        value.let(arg::parse)
            .also(arg::validateUnsafe)
            .also {
                if (pool.args.last() === arg && pool.isLastVararg)
                    varargValues.add(it)
                else
                    values[arg] = it
            }
        argIndex = Math.min(argIndex + 1, if (pool.isLastVararg) pool.args.size - 1 else pool.args.size)
    }

    while (currentNonWhitespace() != null) {
        when {
            current == '-' && !ignoreOptions -> when (next()) {
                '-' -> {
                    /*
                     * A double hyphen delimiter may be followed by either
                     *
                     * 1) a whitespace character, in which case the sequence is interpreted as option parsing terminator, or
                     * 2) an alphabetic literal, in which case the literal is interpreted as long option token.
                     */
                    next()
                    when {
                        current!!.isWhitespace() -> ignoreOptions = true
                        else -> {
                            val cL = currentLiteral { it == '='} ?: throw ParsingException("Unexpected character: $current")
                            if (!cL.all(Char::isAlphanumeric)) throw ParsingException("A long option token must only consist of alphanumeric characters ($cL)")

                            val option = pool.lOptions[cL] ?: throw ParsingException("Unrecognized long option token: \"$cL\"")
                            if (option in values) throw ParsingException("Duplicate option: $option")

                            when {
                                current === null -> null
                                current == '=' -> {
                                    if (option.isMarkerOnly) throw ParsingException("$option does not accept a value")

                                    nextString()
                                }
                                option.isMarkerOnly -> null
                                current!!.isWhitespace() -> when (next()) {
                                    '-' -> {
                                        if (option.hasMarkerValue())
                                            null
                                        else
                                            currentString()
                                    }
                                    else -> currentString()
                                }
                                else -> null
                            }?.apply {
                                option.parse(this).apply {
                                    option.validateUnsafe(this)
                                    values[option] = this
                                }
                            } ?: if (option.hasMarkerValue()) values[option] = option.markerValue else throw ParsingException("$option requires a value to be specified")
                        }
                    }
                }
                else -> {
                    /*
                     * A single hyphen delimiter may be followed by either
                     *
                     * 1) an alphabetic literal, in which case the literal is interpreted as chain of short option tokens, or
                     * 2) a numeric literal, in which case the literal is interpreted as negative number.
                     */
                    val cL = currentLiteral { it == '='} ?: throw ParsingException("Unexpected character: $current")
                    println(cL)

                    when {
                        cL.all(Char::isAlphabetic) -> {
                            if (pool.sOptions === null) throw ParsingException("No short option tokens have been registered")

                            val options = cL.map { pool.sOptions[it] ?: throw ParsingException("Unrecognized short option token: \"$it\"") }
                            options.forEachIndexed { i, option ->
                                if (option in values || options.filterIndexed { index, _ -> i != index }.any { it === option })
                                    throw ParsingException("Duplicate option: $option")
                            }
                            if (options.any(Option<*>::isMarkerOnly) && options.any { !it.hasMarkerValue() })
                                throw ParsingException("")

                            when {
                                current === null -> null
                                current == '=' -> {
                                    if (options.any(Option<*>::isMarkerOnly)) throw ParsingException("$cL contains an option that does not accept a value")

                                    nextString()
                                }
                                current!!.isWhitespace() -> when (next()) {
                                    '-'     -> if (options.all(Option<*>::hasMarkerValue)) null else currentString()
                                    else    -> if (options.any(Option<*>::isMarkerOnly)) null else currentString()
                                }
                                else -> null
                            }?.apply {
                                options.forEach {
                                    it.parse(this).apply {
                                        it.validateUnsafe(this)
                                        values[it] = this
                                    }
                                }
                            } ?: options.forEach { if (it.hasMarkerValue()) values[it] = it.markerValue else throw ParsingException("$it requires a value to be specified") }
                        }
                        cL.all(Char::isNumeric) -> {
                            if (pool.args.isEmpty()) throw ParsingException("No arguments have been registered")
                            appendArgument("-$cL")
                        }
                    }
                }
            }
            else -> {
                // Argument
                if (pool.args.isEmpty()) throw ParsingException("No arguments have been registered")

                val arg = pool.args[argIndex]
                currentString()?.let(arg::parse)
                    .also(arg::validateUnsafe)
                    .also {
                        if (pool.args.last() === arg && pool.isLastVararg)
                            varargValues.add(it)
                        else
                            values[arg] = it
                    } ?: throw ParsingException("Could not parse argument value")
                argIndex = Math.min(argIndex + 1, if (pool.isLastVararg) pool.args.size - 1 else pool.args.size)
            }
        }
    }

    if (argIndex < pool.firstOptionalArg && !(pool.isLastVararg && argIndex == pool.args.size - 1))
        throw ParsingException("Not all required arguments have been specified")

    if (_varargValues.isInitialized()) values[pool.args.last()] = varargValues
    return OptionSet(pool, values)
}

@Suppress("UNCHECKED_CAST")
private inline fun <VT> Argument<VT>.validateUnsafe(value: Any?) = this.validate(value as VT?)

@Suppress("UNCHECKED_CAST")
private inline fun <VT> Option<VT>.validateUnsafe(value: Any?) = this.validate(value as VT?)
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

private val PATTERN_ALPHANUMERIC = "[A-Za-z0-9]+".toRegex()

private val Char.isAlphanumeric: Boolean get() = (this in 'A'..'Z' || this in 'a'..'z' || this in '0'..'9')
private val Char.isNotAlphanumeric: Boolean get() = !isAlphanumeric

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

    fun currentNonWhitespace() =
        if (current !== null && !current!!.isWhitespace()) current else nextNonWhitespace()

    while (currentNonWhitespace() != null) {
        when (current) {
            '-' -> when (next()) {
                '-' -> {
                    // Option by long token
                    val lToken = nextLiteral { it == '=' }?.let { assertAlphanumeric(it) } ?: throw ParsingException("Option token required")
                    val opt = pool.lOptions[lToken] ?: throw ParsingException("Unrecognized long option token: \"$lToken\"")
                    if (opt in values) throw ParsingException("Duplicate option: $opt")

                    when (current) {
                        '='         -> nextString()
                        ' '         -> if (opt.isMarkerOnly || (opt.hasMarkerValue() && nextNonWhitespace() == '-')) null else currentString()
                        else        -> null
                    }?.apply {
                        if (opt.isMarkerOnly) throw ParsingException("$opt must be used as marker option")

                        opt.parse(this).apply {
                            opt.validateUnsafe(this)
                            values[opt] = this
                        }
                    } ?: let { if (opt.hasMarkerValue()) values[opt] = opt.markerValue else throw ParsingException("$opt requires a value to be specified") }
                }
                else -> {
                    // Option/s by short token/s
                    if (pool.sOptions === null) throw ParsingException("No short option tokens have been registered")
                    if (!current!!.isAlphanumeric) throw ParsingException("Option tokens must only contain alphanumeric characters")

                    val opts = currentLiteral { it == '=' }?.let { assertAlphanumeric(it) }?.map { pool.sOptions[it] ?: throw ParsingException("Unrecognized short option token: \"$it\"") } ?: throw ParsingException("Option token required")
                    opts.forEach { if (it in values) throw ParsingException("Duplicate option: $it") }

                    when (current) {
                        '='         -> nextString()
                        ' '         -> if (opts.any(Option<*>::isMarkerOnly) || (opts.any(Option<*>::hasMarkerValue) && nextNonWhitespace() == '-')) null else currentString()
                        else        -> null
                    }?.apply {
                        opts.find(Option<*>::isMarkerOnly)?.let { throw ParsingException("$it must be used as marker option") }
                        opts.forEach {
                            it.parse(this).apply {
                                it.validateUnsafe(this)
                                values[it] = this
                            }
                        }
                    } ?: let { opts.forEach { if (it.hasMarkerValue()) values[it] = it.markerValue else throw ParsingException("$it requires a value to be specified") } }
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

private inline fun assertAlphanumeric(s: String) = s.apply {
    if (!PATTERN_ALPHANUMERIC.matches(s)) throw ParsingException("All characters of string '$this' must be alphanumeric")
}
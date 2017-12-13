@file:JvmName("OptionParser")
@file:Suppress("NOTHING_TO_INLINE")
package com.github.themrmilchmann.kopt

import kotlin.jvm.*
import kotlin.math.*

private val PATTERN_ALPHANUMERIC = "[^A-Za-z0-9]".toRegex()

private val Char.isAlphanumeric: Boolean get() = (this in 'A'..'Z' || this in 'a'..'z' || this in '0'..'9')

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

    nextNonWhitespace()
    while (current != null) {
        when (current) {
            '-' -> when (next()) {
                '-' -> {
                    // Option by long token
                    val lToken = nextLiteral()?.let { assertAlphanumeric(it) } ?: throw ParsingException("Option token required")
                    val opt = pool.lOptions[lToken] ?: throw ParsingException("Unrecognized long option token: \"$lToken\"")
                    if (opt in values) throw ParsingException("Duplicate option: $opt")

                    when (current) {
                        ' ', '=' -> nextString()
                        else -> nextNonWhitespace().let { null }
                    }?.also {
                        opt.parse(it).also {
                            opt.validateUnsafe(it)
                            values[opt] = it
                        }
                    } ?: if (opt.isValueRequired) throw ParsingException("$opt requires a value to be set explicitly")
                }
                else -> {
                    // Option/s by short token/s
                    if (pool.sOptions === null) throw ParsingException("No short option tokens have been registered")
                    if (!current!!.isAlphanumeric) throw ParsingException("Option tokens must only contain alphanumeric characters")

                    val opts = currentLiteral()?.map { pool.sOptions[it] ?: throw ParsingException("Unrecognized short option token: \"$it\"") } ?: throw ParsingException("Option token required")
                    opts.forEach { if (it in values) throw ParsingException("Duplicate option: $it") }

                    when (current) {
                        ' ', '=' -> nextString()
                        else -> nextNonWhitespace().let { null }
                    }?.apply {
                        opts.forEach {
                            it.parse(this).apply {
                                it.validateUnsafe(this)
                                values[it] = this
                            }
                        }
                    } ?: opts.find(Option<*>::isValueRequired)?.let { throw ParsingException("$it requires a value to be set explicitly") }
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
                argIndex = min(argIndex + 1, if (pool.isLastVararg) pool.args.size - 1 else pool.args.size)
                nextNonWhitespace()
            }
        }
    }

    if (argIndex < pool.firstOptionalArg) throw ParsingException("Not all required arguments have been specified")

    if (_varargValues.isInitialized()) values[pool.args.last()] = varargValues
    return OptionSet(pool, values)
}

@Suppress("UNCHECKED_CAST")
private inline fun <VT> Argument<VT>.validateUnsafe(value: Any?) = this.validate(value as VT?)

@Suppress("UNCHECKED_CAST")
private inline fun <VT> Option<VT>.validateUnsafe(value: Any?) = this.validate(value as VT?)

private inline fun assertAlphanumeric(s: String) = s.apply {
    if (PATTERN_ALPHANUMERIC.matches(s)) throw ParsingException("All characters of string '$this' should be alphanumeric")
}
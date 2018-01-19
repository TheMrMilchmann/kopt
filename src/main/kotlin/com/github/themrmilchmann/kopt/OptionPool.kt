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
package com.github.themrmilchmann.kopt

import java.util.*

/**
 * An `OptionPool` is a pool of available [Argument]s and [Option]s.
 *
 * @param args              the arguments available in this pool
 * @param lOptions          the options available in this pool (by long tokens)
 * @param sOptions          the options available in this pool (by short tokens)
 * @param firstOptionalArg  the index of the first optional argument
 * @param isLastVararg      `true` if the last argument is a vararg argument
 *
 * @since 1.0.0
 *
 * @author Leon Linhart <themrmilchmann@gmail.com>
 */
class OptionPool private constructor(
    internal val args: List<Argument<*>>,
    internal val lOptions: Map<String, Option<*>>,
    internal val sOptions: Map<Char, Option<*>>?,
    val firstOptionalArg: Int,
    val isLastVararg: Boolean
) {

    /**
     * Returns whether or not the given [Argument] is in this pool.
     *
     * @param arg the argument to do the check for
     *
     * @return `true` if the given argument is in this pool, or `false`
     *         otherwise
     *
     * @since 1.0.0
     */
    operator fun contains(arg: Argument<*>) = arg in args

    /**
     * Returns whether or not the given [Option] is in this pool.
     *
     * @param opt the option to do the check for
     *
     * @return `true` if the given option is in this pool, or `false` otherwise
     *
     * @since 1.0.0
     */
    operator fun contains(opt: Option<*>) = lOptions.containsValue(opt)

    /**
     * Returns the index of the given `arg` or `-1` if the specified argument is
     * not contained in this pool.
     *
     * @param arg the argument whose index is to be queried
     *
     * @return the index of the given argument or `-1`
     *
     * @since 1.0.0
     */
    fun indexOf(arg: Argument<*>) = args.indexOf(arg)

    /**
     * A builder for an [OptionPool].
     *
     * @since 1.0.0
     */
    class Builder {

        private val _args: Lazy<MutableList<Argument<*>>> = Lazy(::ArrayList)
        private val args: MutableList<Argument<*>> get() = _args.value
        private var firstOptionalArg = Int.MAX_VALUE
        private var isLastVararg = false

        private val _lOptions: Lazy<MutableMap<String, Option<*>>> = Lazy(::HashMap)
        private val lOptions: MutableMap<String, Option<*>> get() = _lOptions.value

        private val _sOptions: Lazy<MutableMap<Char, Option<*>>> = Lazy(::HashMap)
        private val sOptions: MutableMap<Char, Option<*>> get() = _sOptions.value

        /**
         * Returns a new immutable `OptionPool`.
         *
         * Calling this function does not change the state of the builder. Thus,
         * this function may be called multiple times subsequently to receive
         * multiple identical pools.
         *
         * @return a newly created immutable `OptionPool`
         *
         * @since 1.0.0
         */
        fun create() = OptionPool(
            if (_args.isInitialized()) args else Collections.emptyList(),
            if (_lOptions.isInitialized()) lOptions else Collections.emptyMap(),
            if (_sOptions.isInitialized()) sOptions else null,
            if (firstOptionalArg == Int.MAX_VALUE) args.size else firstOptionalArg,
            isLastVararg
        )

        /**
         * Adds the given [Argument] to the pool of available arguments.
         *
         * @param arg the argument to be added
         *
         * @return this builder instance
         *
         * @throws IllegalArgumentException if the specified argument has
         *                                  already been added, a vararg
         *                                  argument has been added, or a
         *                                  required argument is preceded by an
         *                                  optional one
         *
         * @since 1.0.0
         */
        fun withArg(arg: Argument<*>): Builder {
            if (arg in args) throw IllegalArgumentException("Duplicate argument: $arg")
            if (isLastVararg) throw IllegalArgumentException("A vararg argument may not be followed by other arguments")
            if (firstOptionalArg != Int.MAX_VALUE && !arg.isOptional) throw IllegalArgumentException("A required argument must not be preceded by an optional one")

            args.add(arg)
            if (arg.isOptional && firstOptionalArg == Int.MAX_VALUE) firstOptionalArg = args.indexOf(arg)

            return this
        }

        /**
         * Adds the given [Argument] as vararg argument to the pool of available
         * arguments.
         *
         * @param arg the argument to be added as vararg argument
         *
         * @return this builder instance
         *
         * @throws IllegalArgumentException if the specified argument has
         *                                  already been added, a vararg
         *                                  argument has been added, or a
         *                                  required argument is preceded by an
         *                                  optional one
         *
         * @since 1.0.0
         */
        fun withVararg(arg: Argument<*>): Builder {
            if (arg in args) throw IllegalArgumentException("Duplicate argument: $arg")
            if (isLastVararg) throw IllegalArgumentException("A vararg argument may not be followed by other arguments")
            if (firstOptionalArg != Int.MAX_VALUE && !arg.isOptional) throw IllegalArgumentException("A required argument must not be preceded by an optional one")

            args.add(arg)
            isLastVararg = true

            return this
        }

        /**
         * Adds the given [Option] to the pool of available options.
         *
         * @param opt the option to be added
         *
         * @return this builder instance
         *
         * @throws IllegalArgumentException if the given option has already been
         *                                  added, or one of its tokens clashes
         *                                  with a token of an option that has
         *                                  been added before
         *
         * @since 1.0.0
         */
        fun withOption(opt: Option<*>): Builder {
            if (lOptions.containsKey(opt.longToken)) throw IllegalArgumentException("Duplicate long option token: \"${opt.longToken}\"")

            val sToken = opt.shortToken
            if (sToken !== null) {
                if (sOptions.containsKey(sToken)) throw IllegalArgumentException("Duplicate short option token: \"$sToken\"")
                sOptions.put(sToken, opt)
            }

            lOptions.put(opt.longToken, opt)

            return this
        }

    }

}

private object UNINITIALIZED_VALUE

internal class Lazy<out T>(private var initializer: (() -> T)?) {

    private var _value: Any? = UNINITIALIZED_VALUE

    val value: T
        get() {
            if (_value === UNINITIALIZED_VALUE) {
                _value = initializer!!()
                initializer = null
            }

            @Suppress("UNCHECKED_CAST")
            return _value as T
        }

    fun isInitialized(): Boolean = _value !== UNINITIALIZED_VALUE

}
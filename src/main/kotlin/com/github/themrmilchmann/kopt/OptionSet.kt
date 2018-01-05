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

/**
 * An [OptionSet] represents a collection of [Argument]s and [Option]s
 * associated with their values.
 *
 * An `OptionSet` is always tied to an [OptionPool]. All methods that have an
 * `Argument` or `Option` parameter throw on error if the passed value is not
 * available for this set, that is, this sets pool does not contain the argument
 * or option, unless explicitly stated.
 *
 * @see OptionPool
 * @see OptionParser
 *
 * @since 1.0.0
 *
 * @author Leon Linhart <themrmilchmann@gmail.com>
 */
class OptionSet internal constructor(
    val pool: OptionPool,
    private val values: Map<Any, Any?>
) {

    /**
     * Returns the explicitly set value for the given [Argument], or `null` if
     * no value has been set explicitly.
     *
     * **NOTE:** Default values are not explicitly set values.
     *
     * @param [VT]  the type of the arguments value
     * @param arg   the argument whose value is to be queried
     *
     * @return the explicitly set value for the given argument, or `null`

     * @throws IllegalArgumentException if the given argument is not available
     *                                  for this set
     *
     * @since 1.0.0
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <VT> get(arg: Argument<VT>): VT? {
        arg.assertAvailable()
        arg.assertNoVararg()
        return values[arg] as? VT
    }

    /**
     * Returns the explicitly set value for the given [Option], or `null` if no
     * value has been set explicitly
     *
     * **NOTE:** Default values are not explicitly set values.
     *
     * @param [VT]  the type of the options value
     * @param opt   the option whose value is to be queried
     *
     * @return the explicitly set value for the given option, or `null`
     *
     * @throws IllegalArgumentException if the given option is not available for
     *                                  this set
     *
     * @since 1.0.0
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <VT> get(opt: Option<VT>): VT? {
        opt.assertAvailable()
        return values[opt] as? VT
    }

    /**
     * Returns the explicitly set value for the given [Argument], the default
     * value of the argument if no value has been set explicitly, or `null` if
     * neither of the previous ones has been set.
     *
     * @param [VT]  the type of the arguments value
     * @param arg   the argument whose value is to be queried
     *
     * @return the explicitly set value for the given argument, its default
     *         value, or `null`
     *
     * @throws IllegalArgumentException if the given argument is not available
     *                                  for this set
     *
     * @since 1.0.0
     */
    @Suppress("UNCHECKED_CAST")
    fun <VT> getOrDefault(arg: Argument<VT>): VT? = getOrDefault(arg, null)

    /**
     * Returns the explicitly set value for the given [Option], the default
     * value of the option if no value has been set explicitly, or `null` if
     * neither of the previous ones has been set.
     *
     * @param [VT]  the type of the options value
     * @param opt   the option whose value is to be queried
     *
     * @return the explicitly set value for the given option, its default value,
     *         or `null`
     *
     * @throws IllegalArgumentException if the given option is not available for
     *                                  this set
     *
     * @since 1.0.0
     */
    @Suppress("UNCHECKED_CAST")
    fun <VT> getOrDefault(opt: Option<VT>): VT? = getOrDefault(opt, null)

    /**
     * Returns the explicitly set value for the given [Argument], the default
     * value of the given argument if no value has been set explicitly, or the
     * given alternative.
     *
     * @param [VT]  the type of the arguments value
     * @param arg   the argument whose value is to be queried
     * @param alt   the alternate value to be returned
     *
     * @return the explicitly set value for the given argument, its default
     *         value, or the given alternative
     *
     * @throws IllegalArgumentException if the given argument is not available
     *                                  for this set
     *
     * @since 1.0.0
     */
    @Suppress("UNCHECKED_CAST")
    fun <VT> getOrDefault(arg: Argument<VT>, alt: VT?): VT? {
        arg.assertAvailable()
        arg.assertNoVararg()
        return when (arg) {
            in values -> values[arg] as? VT
            else -> if (arg.hasDefault()) arg.defaultValue else alt
        }
    }

    /**
     * Returns the explicitly set value for the given [Option], the default
     * value of the given option if no value has been set explicitly, or the
     * given alternative.
     *
     * @param [VT]  the type of the options value
     * @param opt   the option whose value is to be queried
     * @param alt   the alternate value to be returned
     *
     * @return the explicitly set value for the given option, its default value,
     * or the given alternative
     *
     * @throws IllegalArgumentException if the given option is not available for
     *                                  this set
     *
     * @since 1.0.0
     */
    @Suppress("UNCHECKED_CAST")
    fun <VT> getOrDefault(opt: Option<VT>, alt: VT?): VT? {
        opt.assertAvailable()
        return when (opt) {
            in values -> values[opt] as? VT
            else -> if (opt.hasDefaultValue()) opt.defaultValue else alt
        }
    }

    /**
     * Returns the values set for the underlying [OptionPool]'s vararg argument.
     *
     * @param [VT]  the type of the arguments value
     * @param arg the argument whose value is to be queried
     *
     * @return the values set for the given argument
     *
     * @throws IllegalArgumentException if the given argument is not available
     *                                  for this set, or if the given argument
     *                                  is not a vararg argument for this set
     *
     * @since 1.0.0
     */
    @Suppress("UNCHECKED_CAST")
    fun <VT> getVarargValues(arg: Argument<VT>): Collection<VT?> {
        arg.assertAvailable()
        arg.assertVararg()
        return when (arg) {
            in values -> values[arg] as Collection<VT?>
            else -> if (arg.hasDefault()) listOf(arg.defaultValue) else emptyList()
        }
    }

    /**
     * Returns whether or not the given [Argument] has an explicitly set value
     * in this set.
     *
     * **NOTE:** Default values are not explicitly set values.
     *
     * @param arg the argument whose value is to be queried
     *
     * @return `true` if the given argument has an explicitly set value in this
     *         set, or `false` otherwise
     *
     * @throws IllegalArgumentException if the given argument is not available
     *                                  for this set
     *
     * @since 1.0.0
     */
    @JvmName("hasExplicitlySetValue")
    operator fun contains(arg: Argument<*>): Boolean {
        arg.assertAvailable()
        return arg in values
    }

    /**
     * Returns whether or not the given [Option] has an explicitly set value in
     * this set.
     *
     * **NOTE:** Default values are not explicitly set values.
     *
     * @param opt the argument whose value is to be queried
     *
     * @return `true` if the given option has an explicitly set value in this
     *         set, or `false` otherwise
     *
     * @throws IllegalArgumentException if the given option is not available for
     *                                  this set
     *
     * @since 1.0.0
     */
    @JvmName("hasExplicitlySetValue")
    operator fun contains(opt: Option<*>): Boolean {
        opt.assertAvailable()
        return opt in values
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Argument<*>.isVararg() = pool.isLastVararg && this === pool.args.last()

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Argument<*>.assertVararg() { if (!isVararg()) throw IllegalArgumentException("Argument is not a vararg argument for this set: $this") }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Argument<*>.assertNoVararg() { if (isVararg()) throw IllegalArgumentException("Argument is a vararg argument for this set: $this") }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Argument<*>.assertAvailable() { if (this !in pool) throw IllegalArgumentException("Argument is not available for this set: $this") }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Option<*>.assertAvailable() { if (this !in pool) throw IllegalArgumentException("Option is not available for this set: $this") }

}
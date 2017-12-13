package com.github.themrmilchmann.kopt

/**
 * An `OptionPool` is a pool of available [Argument]s and [Option]s.
 *
 * @param [args] the arguments available in this pool
 * @param [lOptions] the options available in this pool (by long tokens)
 * @param [sOptions] the options available in this pool (by short tokens)
 * @param [firstOptionalArg] the index of the first optional argument
 * @param [isLastVararg] `true` if the last argument is a vararg argument
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
     * @param [arg] the argument to do the check for
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
     * @param [opt] the option to do the check for
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
     * @param [arg] the argument whose index is to be queried
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

        private val _args: Lazy<MutableList<Argument<*>>> = lazy(::mutableListOf)
        private val args by _args
        private var firstOptionalArg = Int.MAX_VALUE
        private var isLastVararg = false

        private val _lOptions: Lazy<MutableMap<String, Option<*>>> = lazy(::mutableMapOf)
        private val lOptions by _lOptions

        private val _sOptions: Lazy<MutableMap<Char, Option<*>>> = lazy(::mutableMapOf)
        private val sOptions by _sOptions

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
            if (_args.isInitialized()) args else emptyList(),
            if (_lOptions.isInitialized()) lOptions else emptyMap(),
            if (_sOptions.isInitialized()) sOptions else null,
            if (firstOptionalArg == Int.MAX_VALUE) args.size else firstOptionalArg,
            isLastVararg
        )

        /**
         * Adds the given [Argument] to the pool of available arguments.
         *
         * @param [arg] the argument to be added
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
        fun withArg(arg: Argument<*>) = apply {
            if (arg in args) throw IllegalArgumentException("Duplicate argument: $arg")
            if (isLastVararg) throw IllegalArgumentException("A vararg argument may not be followed by other arguments")
            if (firstOptionalArg != Int.MAX_VALUE && !arg.isOptional) throw IllegalArgumentException("A required argument must not be preceded by an optional one")

            args.add(arg)
            if (arg.isOptional && firstOptionalArg == Int.MAX_VALUE) firstOptionalArg = args.indexOf(arg)
        }

        /**
         * Adds the given [Argument] as vararg argument to the pool of available
         * arguments.
         *
         * @param [arg] the argument to be added as vararg argument
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
        fun withVararg(arg: Argument<*>) = apply {
            if (arg in args) throw IllegalArgumentException("Duplicate argument: $arg")
            if (isLastVararg) throw IllegalArgumentException("A vararg argument may not be followed by other arguments")
            if (firstOptionalArg != Int.MAX_VALUE && !arg.isOptional) throw IllegalArgumentException("A required argument must not be preceded by an optional one")

            args.add(arg)
            isLastVararg = true
        }

        /**
         * Adds the given [Option] to the pool of available options.
         *
         * @param [opt] the option to be added
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
        fun withOption(opt: Option<*>) = apply {
            if (opt.longToken in lOptions) throw IllegalArgumentException("Duplicate long option token: \"${opt.longToken}\"")

            opt.shortToken?.let {
                if (opt.shortToken in sOptions) throw IllegalArgumentException("Duplicate short option token: \"$it\"")
                sOptions[it] = opt
            }

            lOptions[opt.longToken] = opt
        }

    }

}
[![License](https://img.shields.io/badge/license-BSD-blue.svg)](https://github.com/TheMrMilchmann/kopt/blob/master/LICENSE.md)
[![Build Status](https://travis-ci.org/TheMrMilchmann/kopt.svg?branch=master)](https://travis-ci.org/TheMrMilchmann/kopt)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.themrmilchmann.kopt/kopt/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.themrmilchmann.kopt/kopt)

kopt
====

kopt is a small command line parser library for the JVM.

*kopt is fully compatible with Java 8 and Kotlin 1.1 (or later).*

Specification
-------------

1. **Command line**<br>
    
    1. **Terminating option parsing**<br>
        A double hyphen delimiter followed by a whitespace character
        (e.g. `-- `) terminates option parsing. Thus, section (3) is effectively
        irrelevant for anything that is parsed after this character sequence
        and even literals prefixed with hyphens are interpreted as arguments. 

1. **Arguments**<br>
    Arguments are interpreted index-based.

    1. **Optional arguments**<br>
        An argument may be optional, that is, it is not required to pass a value for said argument.
    
        1. An optional argument must only be followed by other optional arguments.
    
    1. **Vararg arguments**<br>
        The trailing argument may be a vararg argument, that is, 
    
        1. A vararg argument must always be the trailing argument.
        1. A vararg argument may be optional.

1. **Options**<br>
    Options are interpreted key-based.
    
    1. **Option tokens**
        An option token is used to identify an option. Two different kinds of
        tokens exist.
        - Option tokens are case-sensitive.
    
        1. **Long token**<br>
            *A long token is an alphanumeric literal.*
            
            An alphanumeric literal prefixed with a double hyphen delimiter
            (`--`) is interpreted as long option token.
    
        1. **Short token**<br>
            *A short token is an alphabetic character.*
            
            An alphabetic character prefixed with a single hyphen delimiter
            (`-`) is interpreted as short option token.
    
            Multiple short tokens may be chained behind a single hyphen
            delimiter. Thus, `-a -b -c` and `-abc` are equivalent. (Furthermore,
            `-a <value> -b <value> -c <value>` and `-abc <value>` are
            equivalent.)
            Short tokens may only be chained if the way the represented options
            parse values do not conflict.
            + Default options and marker options must be followed by a value.
            + Marker options and marker-only options may not be followed by a
              value.
            - **Default options and marker-only options may not be chained.**

            An alphabetic literal prefixed with a single hyphen delimiter
            (`-`) is interpreted as a set of chained short option tokens.
            
        If an option token and a following string are separated by either an
        equals sign (`=`) or a single whitespace character (e.g. a space ` `),
        the string is interpreted as a value for the option represented by the
        token.
    
    1. **Marker options**<br>
        An option may be a marker option, that is, the option does not require a
        value to be passed and will instead use a predefined value if the option is
        present and no value is passed.<br>
        Additionally, an option may be a marker-only option, that is, the option
        does not accept a value to be passed.
        
Grammar
-------

### Notation
This section informally explains the grammar notation used below.

#### Symbols and naming
- Terminal symbol names start with an uppercase letter, e.g. String.
- Nonterminal symbol names start with lowercase letter, e.g. commandLine.
- Each production starts with a colon (:).
- Symbol definitions may have many productions and are terminated by a semicolon (;).
- Symbol definitions may be prepended with attributes, e.g. start attribute denotes a start symbol.

#### EBNF expressions
- Operator `|` denotes *alternative*.
- Operator `*` denotes *iteration* (zero or more).
- Operator `+` denotes *iteration* (one or more).
- Operator `?` denotes *option* (zero or one).

### Syntax

```
start
commandLine
    : argument+ ("--" string*)?
    ;

argument
    : string
    : option
    ;
    
option
    : defaultOption
    : markerOption
    : markerOnlyOption
    ;
    
defaultOption
    : "-" ShortToken+ "=" String
    : "-" ShortToken+ " " String
    : "--" LongToken "=" String
    : "--" LongToken " " String
    ;
    
markerOption
    : defaultOption
    : markerOnlyOption
    ;

markerOnlyOption
    : "-" ShortToken+
    : "--" LongToken
    ;

ShortOptionToken
    : <any alphabetic character>
    ;
    
Literal
    : <any non-whitespace character>+
    ;

String
    : Literal
    : "\"" <any character>* "\""
    ;
```
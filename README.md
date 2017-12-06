[![License](https://img.shields.io/badge/license-BSD-blue.svg)](https://github.com/TheMrMilchmann/kopt/blob/master/LICENSE.md)
[![Build Status](https://travis-ci.org/TheMrMilchmann/kopt.svg?branch=master)](https://travis-ci.org/TheMrMilchmann/kopt)

kopt
====

kopt is a small framework for platform independent library for reading command
line arguments.

Command Line Argument Convention
--------------------------------

The convention used by kopt's parser is a modified version of the [GNU convention](https://www.gnu.org/prep/standards/html_node/Command_002dLine-Interfaces.html).
The most significant differences are:

* Options are not allowed to appear multiple times.

* Options and their values must be separated by either a single space character
  (` `) or an equals character (`=`).

* Using the double hyphen delimiter (`--`) to terminate option parsing is not
(yet) supported.

**The full convention looks as follows:**

* Arguments are options if they begin with a hyphen delimiter (`-`).

* Option tokens must only contain alphanumeric characters.
    * _short_ option tokens are single alphanumeric characters, and
    * _long_ option tokens are sequences of alphanumeric characters of an
      arbitrary length.

* A character following a single hyphen (`-`) is interpreted as short option
  token.
    * short option tokens may be linked. Thus, `-abc` is equivalent to
      `-a -b -c`.
    
* Characters following a double hyphen delimiter (`--`) are interpreted as long
  option token.
  
* Options may be supplied in any order, but they are not allowed to appear
  multiple times.
  
* Options and their values must be separated by either a single space character
  (` `) or an equals character (`=`).
  
* Arguments may be optional. However, if an argument is optional all following
  arguments must also be optional.
  
* Trailing arguments may be vararg arguments.
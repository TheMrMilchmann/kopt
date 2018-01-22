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
package com.github.themrmilchmann.kopt.test;

import com.github.themrmilchmann.kopt.*;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

@Test
public final class OptionTests {

    public void verifyShortTokenValueParsingExiting() {
        Option<String> a = new Option.Builder<>("a", Parser.STRING).withShortToken('a').create();
        Option<String> b = new Option.Builder<>("b", Parser.STRING).withShortToken('b').create();
        Option<String> c = new Option.Builder<>("c", Parser.STRING).withShortToken('c').create();
        Option<String> d = new Option.Builder<>("d", Parser.STRING).withShortToken('d').create();
        Option<String> e = new Option.Builder<>("e", Parser.STRING).withShortToken('e').create();

        OptionPool pool = new OptionPool.Builder()
                .withOption(a)
                .withOption(b)
                .withOption(c)
                .withOption(d)
                .withOption(e)
                .create();

        OptionSet set = OptionParser.parse(CharTools.streamOf("-a=\"value\" -b \"value\" -c=value -d value -e=\"value\""), pool);

        assertEquals(set.get(a), "value");
        assertEquals(set.get(b), "value");
        assertEquals(set.get(c), "value");
        assertEquals(set.get(d), "value");
        assertEquals(set.get(e), "value");
    }

    public void verifyLongTokenValueParsingExiting() {
        Option<String> a = new Option.Builder<>("a", Parser.STRING).create();
        Option<String> b = new Option.Builder<>("b", Parser.STRING).create();
        Option<String> c = new Option.Builder<>("c", Parser.STRING).create();
        Option<String> d = new Option.Builder<>("d", Parser.STRING).create();
        Option<String> e = new Option.Builder<>("e", Parser.STRING).create();

        OptionPool pool = new OptionPool.Builder()
                .withOption(a)
                .withOption(b)
                .withOption(c)
                .withOption(d)
                .withOption(e)
                .create();

        OptionSet set = OptionParser.parse(CharTools.streamOf("--a=\"value\" --b \"value\" --c=value --d value --e=\"value\""), pool);

        assertEquals(set.get(a), "value");
        assertEquals(set.get(b), "value");
        assertEquals(set.get(c), "value");
        assertEquals(set.get(d), "value");
        assertEquals(set.get(e), "value");
    }

    public void shortTokenMultiAssign() {
        Option<String> a = new Option.Builder<>("unusedA", Parser.STRING).withShortToken('a').create();
        Option<String> b = new Option.Builder<>("unusedB", Parser.STRING).withShortToken('b').create();
        Option<String> c = new Option.Builder<>("unusedC", Parser.STRING).withShortToken('c').create();

        OptionPool pool = new OptionPool.Builder()
                .withOption(a)
                .withOption(b)
                .withOption(c)
                .create();

        OptionSet set = OptionParser.parse(CharTools.streamOf("-abc=\"d\""), pool);

        assertEquals(set.get(a), "d");
        assertEquals(set.get(b), "d");
        assertEquals(set.get(c), "d");

        set = OptionParser.parse(CharTools.streamOf("-abc \"d\""), pool);

        assertEquals(set.get(a), "d");
        assertEquals(set.get(b), "d");
        assertEquals(set.get(c), "d");
    }

    @Test(expectedExceptions = ParsingException.class)
    public void useLongTokenMarkerOnlyWithValue() {
        OptionPool pool = new OptionPool.Builder()
                .withOption(new Option.Builder<>("markerOnly", Parser.INT).withMarkerValue(42, true).create())
                .create();

        OptionParser.parse(CharTools.streamOf("--markerOnly=true"), pool);
    }

    @Test(expectedExceptions = ParsingException.class)
    public void useLongTokenNonMarkerAsMarker() {
        OptionPool pool = new OptionPool.Builder()
                .withOption(new Option.Builder<>("notAMarker", Parser.INT).create())
                .create();

        OptionParser.parse(CharTools.streamOf("--notAMarker"), pool);
    }

    @Test(expectedExceptions = ParsingException.class)
    public void useShortTokenMarkerOnlyWithValue() {
        OptionPool pool = new OptionPool.Builder()
                .withOption(new Option.Builder<>("markerOnly", Parser.INT).withShortToken('m').withMarkerValue(42, true).create())
                .create();

        OptionParser.parse(CharTools.streamOf("-m=true"), pool);
    }

    @Test(expectedExceptions = ParsingException.class)
    public void useShortTokenNonMarkerAsMarker() {
        OptionPool pool = new OptionPool.Builder()
                .withOption(new Option.Builder<>("notAMarker", Parser.INT).withShortToken('n').create())
                .create();

        OptionParser.parse(CharTools.streamOf("-n"), pool);
    }

}
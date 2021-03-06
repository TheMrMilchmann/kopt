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

import java.util.*;
import java.util.function.Function;

import static org.testng.Assert.*;

@Test
public final class ParsingTests {

    private static final Random RNG = new Random(System.currentTimeMillis());

    public void parseBooleans() {
        Argument<Boolean> arg0 = new Argument.Builder<>(Parser.BOOLEAN).create();
        OptionPool pool = new OptionPool.Builder()
                .withArg(arg0)
                .create();

        OptionSet setTrue = OptionParser.parse(CharTools.streamOf("true"), pool);
        assertTrue(Objects.requireNonNull(setTrue.get(arg0)));

        OptionSet setOne = OptionParser.parse(CharTools.streamOf("1"), pool);
        assertTrue(Objects.requireNonNull(setOne.get(arg0)));

        OptionSet setFalse = OptionParser.parse(CharTools.streamOf("0"), pool);
        assertFalse(Objects.requireNonNull(setFalse.get(arg0)));

        OptionSet setZero = OptionParser.parse(CharTools.streamOf("0"), pool);
        assertFalse(Objects.requireNonNull(setZero.get(arg0)));
    }

    public void parseDouble() {
        Argument<Double> arg0 = new Argument.Builder<>(Parser.DOUBLE).create();
        OptionPool pool = new OptionPool.Builder()
                .withArg(arg0)
                .create();

        for (int i = 0; i < 20; i++) {
            double e = RNG.nextDouble();
            OptionSet set = OptionParser.parse(CharTools.streamOf(String.valueOf(e)), pool);
            assertEquals(set.get(arg0), e);
        }
    }

    public void parseInt() {
        Argument<Integer> arg0 = new Argument.Builder<>(Parser.INT).create();
        OptionPool pool = new OptionPool.Builder()
                .withArg(arg0)
                .create();

        for (int i = 0; i < 20; i++) {
            int e = RNG.nextInt();
            OptionSet set = OptionParser.parse(CharTools.streamOf(e < 0 ? "\"" + e + "\"" : String.valueOf(e)), pool);
            assertEquals(set.get(arg0), Integer.valueOf(e));
        }
    }

    public void parseString() {
        Argument<String> arg0 = new Argument.Builder<>((it) -> it).create();
        OptionPool pool = new OptionPool.Builder()
                .withArg(arg0)
                .create();

        String[] strings = {
            "Wackelpudding",
            "Alles Im Eimer",
            "Noch Mehr \"Tests\"..."
        };

        for (String s : strings) {
            OptionSet set = OptionParser.parse(CharTools.streamOf(s.contains(" ") ? "\"" + s.replace("\"", "\\\"") + "\"" : s.replace("\"", "\\\"")), pool);
            assertEquals(set.get(arg0), s);
        }
    }

    public void parseVararg() {
        Argument<String> arg0 = new Argument.Builder<>((it) -> it).create();
        OptionPool pool = new OptionPool.Builder()
                .withVararg(arg0)
                .create();

        String[] strings = {
            "Wackelpudding",
            "Alles Im Eimer",
            "Noch Mehr \"Tests\"..."
        };

        OptionSet set = OptionParser.parse(CharTools.streamOf(strings), pool);
        assertTrue(Arrays.equals(strings, set.getVarargValues(arg0).toArray()));
    }

    public void parseMarkerFollowedByArg() {
        Argument<String> arg0 = new Argument.Builder<>((it) -> it).create();
        Option<String> opt0 = new Option.Builder<>("test", (it) -> it).withMarkerValue("marker", true).create();
        OptionPool pool = new OptionPool.Builder()
                .withArg(arg0)
                .withOption(opt0)
                .create();

        String[] args = {
            "--test",
            "arg"
        };

        OptionSet set = OptionParser.parse(CharTools.streamOf(args), pool);
        assertEquals(set.get(opt0), "marker");
        assertEquals(set.get(arg0), "arg");
    }

    public void parseRealWorldString() {
        Argument<String> arg0 = new Argument.Builder<>((it) -> it).create();
        Argument<String> arg1 = new Argument.Builder<>((it) -> it).create();
        Option<String> opt0 = new Option.Builder<>("test", (it) -> it).create();
        OptionPool pool = new OptionPool.Builder()
                .withArg(arg0)
                .withVararg(arg1)
                .withOption(opt0)
                .create();

        OptionSet set = OptionParser.parse(CharTools.streamOf("single --test=foobar test1 test2"), pool);
        assertEquals(set.get(arg0), "single");
        assertEquals(set.get(opt0), "foobar");

        String[] expected = { "test1", "test2" };
        Collection<String> values = set.getVarargValues(arg1);
        Iterator<String> itr = values.iterator();

        for (String s : expected) {
            assertEquals(s, itr.next());
        }
    }

    public void parseRealWorldJoinedArray() {
        Argument<String> arg0 = new Argument.Builder<>((it) -> it).create();
        Argument<String> arg1 = new Argument.Builder<>((it) -> it).create();
        Option<String> opt0 = new Option.Builder<>("test", (it) -> it).create();
        OptionPool pool = new OptionPool.Builder()
                .withArg(arg0)
                .withVararg(arg1)
                .withOption(opt0)
                .create();

        String[] args = {
            "single",
            "--test=foobar",
            "test1",
            "test2"
        };

        OptionSet set = OptionParser.parse(CharTools.streamOf(args), pool);
        assertEquals(set.get(arg0), "single");
        assertEquals(set.get(opt0), "foobar");

        String[] expected = { "test1", "test2" };
        Collection<String> values = set.getVarargValues(arg1);
        Iterator<String> itr = values.iterator();

        for (String s : expected) {
            assertEquals(s, itr.next());
        }
    }

}
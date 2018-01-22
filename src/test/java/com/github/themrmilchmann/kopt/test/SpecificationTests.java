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
public final class SpecificationTests {

    public void testShortOptionTokenParsing() {
        Argument<Integer> arg0 = new Argument.Builder<>(Parser.INT).create();
        Option<String> optTest = new Option.Builder<>("test", Parser.STRING).withShortToken('t').create();

        OptionPool pool = new OptionPool.Builder()
            .withArg(arg0)
            .withOption(optTest)
            .create();

        {
            String command = "-1";
            OptionSet set = OptionParser.parse(CharTools.streamOf(command), pool);

            assertEquals(set.get(arg0), Integer.valueOf(-1));
        }

        expectThrows(ParsingException.class, () -> {
            String command = "-f -1";
            OptionParser.parse(CharTools.streamOf(command), pool);
        });

        expectThrows(ParsingException.class, () -> {
            String command = "-f 1";
            OptionParser.parse(CharTools.streamOf(command), pool);
        });
    }

    public void testShortOptionTokenChaining() {
        Argument<String> arg0 = new Argument.Builder<>(Parser.STRING, true).create();
        Option<String> optMarkerOnly = new Option.Builder<>("markerOnly", Parser.STRING).withShortToken('o').withMarkerValue("only", true).create();
        Option<String> optMarker = new Option.Builder<>("marker", Parser.STRING).withShortToken('m').withMarkerValue("marker", false).create();
        Option<String> optFoo = new Option.Builder<>("foo", Parser.STRING).withShortToken('f').create();

        OptionPool pool = new OptionPool.Builder()
            .withArg(arg0)
            .withOption(optMarkerOnly)
            .withOption(optMarker)
            .withOption(optFoo)
            .create();

        expectThrows(ParsingException.class, () -> {
            String command = "-of";
            OptionParser.parse(CharTools.streamOf(command), pool);
        });

        expectThrows(ParsingException.class, () -> {
            String command = "-of bar";
            OptionParser.parse(CharTools.streamOf(command), pool);
        });

        {
            String command = "-mo bar";
            OptionSet set = OptionParser.parse(CharTools.streamOf(command), pool);

            assertEquals(set.get(arg0), "bar");
            assertEquals(set.get(optMarker), "marker");
            assertEquals(set.get(optMarkerOnly), "only");
        }

        {
            String command = "-mf bar";
            OptionSet set = OptionParser.parse(CharTools.streamOf(command), pool);

            assertEquals(set.get(optMarker), "bar");
            assertEquals(set.get(optFoo), "bar");
        }

        {
            String command = "-mo";
            OptionSet set = OptionParser.parse(CharTools.streamOf(command), pool);

            assertEquals(set.get(optMarker), "marker");
            assertEquals(set.get(optMarkerOnly), "only");
        }
    }

    public void testShortOptionTokenChainingWithValue() {
        Option<String> optTest = new Option.Builder<>("test", Parser.STRING).withShortToken('t').withMarkerValue("mv", false).create();
        Option<String> optFoo = new Option.Builder<>("foo", Parser.STRING).withShortToken('f').withMarkerValue("bar", false).create();

        OptionPool pool = new OptionPool.Builder()
            .withOption(optTest)
            .withOption(optFoo)
            .create();

        {
            String command = "-tf bar";
            OptionSet set = OptionParser.parse(CharTools.streamOf(command), pool);

            assertEquals(set.get(optTest), "bar");
            assertEquals(set.get(optFoo), "bar");
        }

        {
            String command = "-tf=bar";
            OptionSet set = OptionParser.parse(CharTools.streamOf(command), pool);

            assertEquals(set.get(optTest), "bar");
            assertEquals(set.get(optFoo), "bar");
        }
    }

    public void testMarkerOptionValueParsing() {
        Option<String> optTest = new Option.Builder<>("test", Parser.STRING).withShortToken('t').withMarkerValue("mv", false).create();

        OptionPool pool = new OptionPool.Builder()
            .withOption(optTest)
            .create();

        {
            String command = "--test=-f";
            OptionSet set = OptionParser.parse(CharTools.streamOf(command), pool);

            assertEquals(set.get(optTest), "-f");
        }

        {
            String command = "--test=--f";
            OptionSet set = OptionParser.parse(CharTools.streamOf(command), pool);

            assertEquals(set.get(optTest), "--f");
        }
    }

    public void testMarkerOptionValueSkipping() {
        Option<String> optTest = new Option.Builder<>("test", Parser.STRING).withShortToken('t').withMarkerValue("mv", false).create();
        Option<String> optFoo = new Option.Builder<>("foo", Parser.STRING).withShortToken('f').withMarkerValue("bar", false).create();

        OptionPool pool = new OptionPool.Builder()
            .withOption(optTest)
            .withOption(optFoo)
            .create();

        {
            String command = "--test -f";
            OptionSet set = OptionParser.parse(CharTools.streamOf(command), pool);

            assertEquals(set.get(optTest), "mv");
            assertEquals(set.get(optFoo), "bar");
        }

        {
            String command = "--test --foo";
            OptionSet set = OptionParser.parse(CharTools.streamOf(command), pool);

            assertEquals(set.get(optTest), "mv");
            assertEquals(set.get(optFoo), "bar");
        }
    }

    public void testMarkerOnlyOptionValueSkipping() {
        Argument<String> arg0 = new Argument.Builder<>(Parser.STRING).create();
        Option<String> optTest = new Option.Builder<>("test", Parser.STRING).withShortToken('t').withMarkerValue("mv", true).create();

        OptionPool pool = new OptionPool.Builder()
            .withArg(arg0)
            .withOption(optTest)
            .create();

        {
            String command = "--test hi";
            OptionSet set = OptionParser.parse(CharTools.streamOf(command), pool);

            assertEquals(set.get(arg0), "hi");
            assertEquals(set.get(optTest), "mv");
        }

        expectThrows(ParsingException.class, () -> {
            String command = "--test=hi";
            OptionParser.parse(CharTools.streamOf(command), pool);
        });
    }

    public void testOptionParsingTermination() {
        Argument<String> arg0 = new Argument.Builder<>(Parser.STRING).create();
        Argument<String> arg1 = new Argument.Builder<>(Parser.STRING).create();

        OptionPool pool = new OptionPool.Builder()
            .withArg(arg0)
            .withArg(arg1)
            .create();

        expectThrows(ParsingException.class, () -> {
            String command = "value0 --value1";
            OptionParser.parse(CharTools.streamOf(command), pool);
        });

        {
            String command = "value0 -- value1";
            OptionSet set = OptionParser.parse(CharTools.streamOf(command), pool);

            assertEquals(set.get(arg0), "value0");
            assertEquals(set.get(arg1), "value1");
        }

        {
            String command = "value0 -- --value1";
            OptionSet set = OptionParser.parse(CharTools.streamOf(command), pool);

            assertEquals(set.get(arg0), "value0");
            assertEquals(set.get(arg1), "--value1");
        }
    }

}
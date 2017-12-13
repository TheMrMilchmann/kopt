package com.github.themrmilchmann.kopt.test;

import com.github.themrmilchmann.kopt.*;
import org.testng.annotations.Test;

import java.util.Random;

import static org.testng.Assert.*;

@Test
public final class ParsingTests {

    private static final Random RNG = new Random(System.currentTimeMillis());

    public void parseBooleans() {
        Argument<Boolean> arg0 = new Argument.Builder<>(Parser.BOOLEAN).create();
        OptionPool pool = new OptionPool.Builder()
                .withArg(arg0)
                .create();

        OptionSet setTrue = OptionParser.parse(CharStreams.streamOf("true"), pool);
        assertTrue(setTrue.get(arg0));

        OptionSet setOne = OptionParser.parse(CharStreams.streamOf("1"), pool);
        assertTrue(setOne.get(arg0));

        OptionSet setFalse = OptionParser.parse(CharStreams.streamOf("0"), pool);
        assertFalse(setFalse.get(arg0));

        OptionSet setZero = OptionParser.parse(CharStreams.streamOf("0"), pool);
        assertFalse(setZero.get(arg0));
    }

    public void parseDouble() {
        Argument<Double> arg0 = new Argument.Builder<>(Parser.DOUBLE).create();
        OptionPool pool = new OptionPool.Builder()
                .withArg(arg0)
                .create();

        for (int i = 0; i < 20; i++) {
            double e = RNG.nextDouble();
            OptionSet set = OptionParser.parse(CharStreams.streamOf(String.valueOf(e)), pool);
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
            OptionSet set = OptionParser.parse(CharStreams.streamOf(e < 0 ? "\"" + e + "\"" : String.valueOf(e)), pool);
            assertEquals((int) set.get(arg0), e);
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
            OptionSet set = OptionParser.parse(CharStreams.streamOf(s.contains(" ") ? "\"" + s.replace("\"", "\\\"") + "\"" : s.replace("\"", "\\\"")), pool);
            assertEquals(set.get(arg0), s);
        }
    }

}
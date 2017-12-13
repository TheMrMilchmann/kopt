package com.github.themrmilchmann.kopt.test;

import com.github.themrmilchmann.kopt.*;
import org.testng.annotations.Test;

@Test
public final class ArgumentTests {

    @Test(expectedExceptions = ParsingException.class)
    public void doNotSatisfyAllRequired() {
        OptionPool pool = new OptionPool.Builder()
                .withArg(new Argument.Builder<>(Parser.BOOLEAN).create())
                .withArg(new Argument.Builder<>(Parser.BOOLEAN).create())
                .withArg(new Argument.Builder<>(Parser.BOOLEAN).create())
                .create();

        OptionParser.parse(CharStreams.streamOf("true true"), pool);
    }

    public void satisfyRequiredOnly() {
        OptionPool pool = new OptionPool.Builder()
                .withArg(new Argument.Builder<>(Parser.BOOLEAN).create())
                .withArg(new Argument.Builder<>(Parser.BOOLEAN).create())
                .withArg(new Argument.Builder<>(Parser.BOOLEAN, true).create())
                .withArg(new Argument.Builder<>(Parser.BOOLEAN, true).create())
                .withArg(new Argument.Builder<>(Parser.BOOLEAN, true).create())
                .create();

        OptionParser.parse(CharStreams.streamOf("true true"), pool);
    }

    public void satisfySomeOptional() {
        OptionPool pool = new OptionPool.Builder()
                .withArg(new Argument.Builder<>(Parser.BOOLEAN).create())
                .withArg(new Argument.Builder<>(Parser.BOOLEAN).create())
                .withArg(new Argument.Builder<>(Parser.BOOLEAN, true).create())
                .withArg(new Argument.Builder<>(Parser.BOOLEAN, true).create())
                .withArg(new Argument.Builder<>(Parser.BOOLEAN, true).create())
                .create();

        OptionParser.parse(CharStreams.streamOf("true true true true"), pool);
    }

}
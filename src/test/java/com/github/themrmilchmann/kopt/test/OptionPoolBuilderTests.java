package com.github.themrmilchmann.kopt.test;

import com.github.themrmilchmann.kopt.Argument;
import com.github.themrmilchmann.kopt.OptionPool;
import com.github.themrmilchmann.kopt.Parser;
import org.testng.annotations.Test;

@Test
public final class OptionPoolBuilderTests {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void addArgumentAfterVararg() {
        new OptionPool.Builder()
                .withVararg(new Argument.Builder<>(Parser.BOOLEAN).create())
                .withArg(new Argument.Builder<>(Parser.BOOLEAN).create());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void addRequiredAfterOptional() {
        new OptionPool.Builder()
                .withArg(new Argument.Builder<>(Parser.BOOLEAN, true).create())
                .withArg(new Argument.Builder<>(Parser.BOOLEAN).create());

    }

}
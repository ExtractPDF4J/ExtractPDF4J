package com.extractpdf4j;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/** Keeps this lightweight: verify usage output with no args. */
class MainTest {
    private final PrintStream origOut = System.out;
    private ByteArrayOutputStream out;

    @BeforeEach void capture() {
        out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
    }
    @AfterEach void restore() {
        System.setOut(origOut);
    }

    @Test
    void printsUsageOnNoArgs() throws Exception {
        Main.main(new String[]{});
        String s = out.toString();
        assertTrue(s.contains("Usage:"));
        assertTrue(s.contains("--mode"));
    }
}

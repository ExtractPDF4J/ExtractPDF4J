package com.extractpdf4j.cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Keeps this lightweight: verify usage output with no args. */
class MainTest {
    private final PrintStream origOut = System.out;
    private final PrintStream origErr = System.err;
    private ByteArrayOutputStream out;
    private ByteArrayOutputStream err;

    @BeforeEach
    void capture() {
        out = new ByteArrayOutputStream();
        err = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
    }

    @AfterEach
    void restore() {
        System.setOut(origOut);
        System.setErr(origErr);
    }

    @Test
    void printsUsageOnNoArgs() throws Exception {
        Main.main(new String[]{});
        String stdout = out.toString();
        String stderr = err.toString();
        assertTrue(stdout.contains("Usage:") || stderr.contains("Usage:"));
        assertTrue(stdout.contains("--mode") || stderr.contains("--mode"));
    }
}

package com.extractpdf4j.cli;

import com.extractpdf4j.helpers.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertTrue(stdout.contains("--json") || stderr.contains("--json"));
    }

    @Test
    void writesSingleJsonDocumentForAllTables(@TempDir Path temporaryDirectory) throws Exception {
        Path output = temporaryDirectory.resolve("tables.json");
        Table table = new Table(List.of(List.of("Name", "Málaga")), List.of(), List.of());

        Main.writeJson(List.of(table), output.toString());

        assertTrue(Files.exists(output));
        assertEquals("{\"tables\":[{\"rows\":[[\"Name\",\"Málaga\"]]}]}", Files.readString(output));
    }
}

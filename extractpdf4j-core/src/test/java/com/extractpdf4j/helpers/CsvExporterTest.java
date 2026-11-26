package com.extractpdf4j.helpers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

class CsvExporterTest {

    @Test
    void testDelimiterValidation() {
        CsvExporter exporter = new CsvExporter();
        
        // Test null
        assertThrows(IllegalArgumentException.class, () -> exporter.setDelimiter(null));
        
        // Test empty string
        assertThrows(IllegalArgumentException.class, () -> exporter.setDelimiter(""));
        
        // Test multiple characters
        assertThrows(IllegalArgumentException.class, () -> exporter.setDelimiter(",,"));
        
        // Test valid single character
        assertDoesNotThrow(() -> exporter.setDelimiter(";"));
        assertDoesNotThrow(() -> exporter.setDelimiter(","));
    }

    @Test
    void testCustomDelimiter() {
        CsvExporter exporter = new CsvExporter();
        exporter.setDelimiter(";");
        
        Table table = new Table(
                List.of(Arrays.asList("Col1", "Col2"), Arrays.asList("Val1", "Val2")),
                List.of(0.0, 10.0, 20.0),
                List.of(0.0, 10.0, 20.0)
        );
        
        String expected = "Col1;Col2" + System.lineSeparator() + "Val1;Val2" + System.lineSeparator();
        assertEquals(expected, exporter.export(Collections.singletonList(table)));
    }

    @Test
    void testValuesContainingDelimiter() {
        CsvExporter exporter = new CsvExporter();
        exporter.setDelimiter(";");
        
        Table table = new Table(
                List.of(Arrays.asList("A;B", "C")),
                List.of(0.0, 10.0, 20.0),
                List.of(0.0, 10.0)
        );
        
        String expected = "\"A;B\";C" + System.lineSeparator();
        assertEquals(expected, exporter.export(Collections.singletonList(table)));
    }

    @Test
    void testValuesContainingQuotes() {
        CsvExporter exporter = new CsvExporter();
        exporter.setDelimiter(",");
        
        Table table = new Table(
                List.of(Arrays.asList("A \"quote\" test", "B")),
                List.of(0.0, 10.0, 20.0),
                List.of(0.0, 10.0)
        );
        
        String expected = "\"A \"\"quote\"\" test\",B" + System.lineSeparator();
        assertEquals(expected, exporter.export(Collections.singletonList(table)));
    }

    @Test
    void testNullCellValues() {
        CsvExporter exporter = new CsvExporter();
        exporter.setDelimiter(",");
        
        Table table = new Table(
                List.of(Arrays.asList("A", null, "C")),
                List.of(0.0, 10.0, 20.0, 30.0),
                List.of(0.0, 10.0)
        );
        
        String expected = "A,,C" + System.lineSeparator();
        assertEquals(expected, exporter.export(Collections.singletonList(table)));
    }

    @Test
    void testCollapsesMultiSpacesTrimsEdgesAndPreservesSingleInternalSpaces() {
        CsvExporter exporter = new CsvExporter();

        Table table = new Table(
                List.of(Arrays.asList(
                        "  leading   and   trailing  ",
                        "\t\tSalary\t\tpayment\u00A0\u00A0",
                        "   \t\u00A0\u2003\u3000  ",
                        "Single internal space"
                )),
                List.of(0.0, 10.0, 20.0, 30.0, 40.0),
                List.of(0.0, 10.0)
        );

        String expected = "leading and trailing,Salary payment,,Single internal space" + System.lineSeparator();
        assertEquals(expected, exporter.export(Collections.singletonList(table)));
        assertEquals("leading and trailing,Salary payment,,Single internal space", table.toCSV(','));
    }

    @Test
    void testQuotesAndNormalizesMultilineCellsAcrossLineEndings() {
        CsvExporter exporter = new CsvExporter();

        Table table = new Table(
                List.of(Arrays.asList("  line   one  \n  line\t\ttwo  ", "  plain  ")),
                List.of(0.0, 10.0, 20.0),
                List.of(0.0, 10.0)
        );

        String expected = "\"line one\nline two\",plain" + System.lineSeparator();
        assertEquals(expected, exporter.export(Collections.singletonList(table)));
    }

    @Test
    void testGoldenCsvDeterministicAcrossLocales() throws IOException {
        Table table = new Table(
                List.of(
                        Arrays.asList("  Date  ", "  Item   Description\t", "  Amount  (USD)  "),
                        Arrays.asList(" 2026-01-15 ", "Salary payment", " 1\u00A0000.00 "),
                        Arrays.asList("2026-01-16", "   Coffee   \t\t  Shop\u00A0\u2003\u3000Branch   ", "   \t \u00A0  "),
                        Arrays.asList("2026-01-17", "  \"ACME,   Corp\"   invoice  ", "  42.50  "),
                        Arrays.asList("2026-01-18", "  Line   one  \n  Line\t\ttwo  ", null)
                ),
                List.of(0.0, 10.0, 20.0, 30.0),
                List.of(0.0, 10.0, 20.0, 30.0, 40.0, 50.0)
        );

        String goldenNormalized;
        try (InputStream stream = Objects.requireNonNull(
                getClass().getResourceAsStream("/csv-whitespace-golden.csv"))) {
            goldenNormalized = new String(stream.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("\r\n", "\n");
        }

        Locale original = Locale.getDefault();
        try {
            for (Locale locale : List.of(Locale.US, Locale.GERMANY, Locale.JAPAN)) {
                Locale.setDefault(locale);
                CsvExporter exporter = new CsvExporter();
                String actual = exporter.export(Collections.singletonList(table));
                assertTrue(actual.endsWith(System.lineSeparator()));
                assertEquals(goldenNormalized, actual.replace("\r\n", "\n"));
                assertEquals(
                        goldenNormalized.substring(0, goldenNormalized.length() - 1),
                        table.toCSV(',').replace("\r\n", "\n")
                );
            }
        } finally {
            Locale.setDefault(original);
        }
    }
}

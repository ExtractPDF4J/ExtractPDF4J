package com.extractpdf4j.helpers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
}

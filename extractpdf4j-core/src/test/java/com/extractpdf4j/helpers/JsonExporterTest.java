package com.extractpdf4j.helpers;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonExporterTest {

    @Test
    void exportsAllTablesAsEscapedJsonRows() {
        Table first = new Table(
                List.of(List.of("Header", "Quoted \"value\""), List.of("Line\nbreak", "\\path")),
                List.of(), List.of());
        Table second = new Table(List.of(List.of("tail", "")), List.of(), List.of());

        assertEquals(
                "{\"tables\":[{\"rows\":[[\"Header\",\"Quoted \\\"value\\\"\"],[\"Line\\nbreak\",\"\\\\path\"]]},{\"rows\":[[\"tail\",\"\"]]}]}",
                JsonExporter.export(List.of(first, second)));
    }

    @Test
    void exportsAnEmptyTableList() {
        assertEquals("{\"tables\":[]}", JsonExporter.export(List.of()));
    }
}

package com.extractpdf4j.parsers;

import com.extractpdf4j.helpers.ExtractionDiagnostics;
import com.extractpdf4j.helpers.ExtractionResult;
import com.extractpdf4j.helpers.ParserSelection;
import com.extractpdf4j.helpers.Table;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiagnosticsTest {
    static class DiagnosticParser extends BaseParser {
        DiagnosticParser() { super("diagnostic.pdf"); }

        @Override
        protected List<Table> parsePage(int page) {
            return List.of(new Table(List.of(List.of("A", "B")), List.of(0.0, 1.0, 2.0), List.of(0.0, 1.0)));
        }

        @Override
        public List<Table> parse(PDDocument document) throws IOException {
            return Collections.emptyList();
        }
    }

    @Test
    void parseResultIncludesDiagnosticsWhenEnabled() throws Exception {
        ExtractionResult result = new DiagnosticParser().diagnostics(true).parseResult();

        assertEquals(1, result.tables().size());
        assertNotNull(result.diagnostics());
        assertEquals(1, result.diagnostics().pagesProcessed());
        assertEquals(1, result.diagnostics().tablesDetected());
    }

    @Test
    void diagnosticsToStringMatchesUserFacingFormat() {
        ExtractionDiagnostics diagnostics = new ExtractionDiagnostics(
                ParserSelection.OCR_STREAM,
                "No usable text layer detected",
                Duration.ofMillis(2110),
                Duration.ofMillis(1740),
                Duration.ofMillis(286),
                4,
                2,
                List.of("Page 3 required OCR fallback", "Table 2 contains inconsistent row widths"),
                Collections.emptyList());

        String text = diagnostics.toString();

        assertTrue(text.contains("Parser selected: OCR_STREAM"));
        assertTrue(text.contains("Reason: No usable text layer detected"));
        assertTrue(text.contains("Pages processed: 4"));
        assertTrue(text.contains("Tables detected: 2"));
        assertTrue(text.contains("OCR duration: 1.74 seconds"));
        assertTrue(text.contains("Parsing duration: 286 ms"));
        assertTrue(text.contains("Total duration: 2.11 seconds"));
        assertTrue(text.contains("- Page 3 required OCR fallback"));
    }
}
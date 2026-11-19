package com.extractpdf4j.parsers;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** Tests post-normalization on real PDF text, without OCR or automatic table detection. */
class OcrStreamParserNormalizationPdfTest {
    @ParameterizedTest(name = "PDF post-normalization: {0}")
    @CsvSource({
            "01-negative-amounts.pdf, 9", "02-locale-decimal-grouping.pdf, 11",
            "03-space-and-apostrophe-grouping.pdf, 9", "04-credit-cr-markers.pdf, 11",
            "05-dd-mmm-yy-dates.pdf, 13", "06-mixed-statement.pdf, 12"
    })
    void normalizesOriginalFixture(String filename, int dataRows) throws Exception {
        List<List<String>> grid = new ArrayList<>();
        try (PDDocument document = PDDocument.load(Path.of("..", "examples", filename).toFile())) {
            assertEquals(1, document.getNumberOfPages());
            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.setSortByPosition(true);
            // Known geometry of these fixtures isolates normalization from segmentation.
            int[] widths = {58, 112, 330, 270};
            for (int r = 0; r <= dataRows; r++) {
                int x = 36;
                for (int c = 0; c < widths.length; c++) {
                    stripper.addRegion(r + ":" + c,
                            new Rectangle2D.Float(x, 118 + r * 25, widths[c], 25));
                    x += widths[c];
                }
            }
            stripper.extractRegions(document.getPage(0));
            for (int r = 0; r <= dataRows; r++) {
                List<String> row = new ArrayList<>();
                for (int c = 0; c < widths.length; c++)
                    row.add(stripper.getTextForRegion(r + ":" + c).strip());
                grid.add(row);
            }
        }
        Method normalize = OcrStreamParser.class.getDeclaredMethod("normalizeNumericLikeColumns", List.class);
        normalize.setAccessible(true);
        normalize.invoke(null, grid);

        Map<String, List<String>> transactions = grid.stream().skip(1)
                .collect(Collectors.toMap(row -> row.get(0), row -> row));
        assertEquals(dataRows, transactions.size());
        List<Executable> checks = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(
                getClass().getResourceAsStream("/normalization-pdf-expectations.tsv")), StandardCharsets.UTF_8))) {
            for (String line : reader.lines().skip(1).toList()) {
                String[] expected = line.split("\t", -1);
                if (!expected[0].equals(filename)) continue;
                checks.add(() -> {
                    List<String> row = transactions.get(expected[1]);
                    assertNotNull(row, "Missing transaction " + expected[1]);
                    int column = expected[2].equals("date") ? 1 : 3;
                    assertEquals(expected[3], row.get(column), expected[1] + " " + expected[2]);
                });
            }
        }
        assertFalse(checks.isEmpty(), "Missing expectations for " + filename);
        assertAll(filename, checks);
    }
}

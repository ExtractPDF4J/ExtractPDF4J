package com.extractpdf4j.parsers;

import com.extractpdf4j.helpers.Table;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Real PDF regressions for statement row order, spacing and independent ruled regions. */
class StatementRegressionTest {
    @TempDir Path temporaryDirectory;

    private static final float[] COLUMNS = {40, 105, 170, 320, 400, 450, 500, 570};
    private static final String[][] TRANSACTIONS = {
            {"Date", "Value Date", "Description", "Reference", "Debit", "Credit", "Balance"},
            {"01-04-2025", "01-04-2025", "Coffee", "REF001", "10.00", "", "990.00"},
            {"02-04-2025", "02-04-2025", "Salary payment", "REF002", "", "200.00", "1190.00"},
            {"03-04-2025", "03-04-2025", "Grocery shopping", "REF003", "20.00", "", "1170.00"},
            {"04-04-2025", "04-04-2025", "Online purchase", "REF004", "30.00", "", "1140.00"}
    };

    @Test
    void streamKeepsSevenColumnsAndVisualRowOrderWithVariableDescriptions() throws Exception {
        try (PDDocument document = statement(false, false)) {
            Table table = new StreamParser().parse(document).get(0);
            assertEquals(List.of(TRANSACTIONS[0]), table.asList().get(0));
            assertEquals(7, table.ncols());
            assertEquals(5, table.nrows());
            for (int row = 0; row < TRANSACTIONS.length; row++) {
                assertEquals(List.of(TRANSACTIONS[row]), table.asList().get(row));
            }
            assertTrue(table.getRowBoundaries().get(0) < table.getRowBoundaries().get(4));
        }
    }

    @Test
    void streamIgnoresOneOffGapAndMatchesFileAndInMemoryPaths() throws Exception {
        try (PDDocument document = statement(false, true)) {
            Path pdf = temporaryDirectory.resolve("statement.pdf");
            document.save(pdf.toFile());
            Table table = new StreamParser().parse(document).get(0);
            assertEquals(7, table.ncols());
            assertEquals(table.asList(), new StreamParser(pdf.toString()).parse().get(0).asList());
            assertEquals("SAMPLE BANK", table.cell(0, 0));
            assertEquals("01-04-2025", table.cell(4, 0));
        }
    }

    @Test
    void latticeSeparatesSummaryAndTransactionsAndPreservesCellText() throws Exception {
        try (PDDocument document = statement(true, true)) {
            List<Table> tables = new LatticeParser().dpi(144).parse(document);
            assertEquals(2, tables.size());
            assertEquals(List.of("Opening", "1000.00"), tables.get(0).asList().get(0));
            Table transactions = tables.get(1);
            assertEquals(7, transactions.ncols());
            assertEquals(5, transactions.nrows());
            for (int row = 0; row < TRANSACTIONS.length; row++) {
                assertEquals(List.of(TRANSACTIONS[row]), transactions.asList().get(row));
            }
            Path pdf = temporaryDirectory.resolve("ruled.pdf");
            document.save(pdf.toFile());
            assertEquals(transactions.asList(), new LatticeParser(pdf.toString()).dpi(144)
                    .parse().get(1).asList());
        }
    }

    @Test
    void latticeDetectsShortTableWithoutPageWideLines() throws Exception {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                grid(content, new float[]{40, 100, 160}, 600, 2);
                text(content, 45, 586, "A"); text(content, 105, 586, "B");
                text(content, 45, 566, "C"); text(content, 105, 566, "D");
            }
            assertEquals(List.of(List.of("A", "B"), List.of("C", "D")),
                    new LatticeParser().dpi(144).parse(document).get(0).asList());
        }
    }

    @Test
    void latticeDoesNotInventGridOnBlankOrUnruledPage() throws Exception {
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage());
            assertTrue(new LatticeParser().dpi(144).parse(document).isEmpty());
        }
        try (PDDocument document = statement(false, false)) {
            assertTrue(new LatticeParser().dpi(144).parse(document).isEmpty());
        }
    }

    @Test
    void hybridPreservesRuledTransactionColumnsWithSummaryAbove() throws Exception {
        try (PDDocument document = statement(true, true)) {
            List<Table> tables = new HybridParser().dpi(144).parse(document);
            Table transactions = tables.stream().filter(table -> table.ncols() == 7)
                    .findFirst().orElseThrow();
            assertEquals(5, transactions.nrows());
            for (int row = 0; row < TRANSACTIONS.length; row++) {
                assertEquals(List.of(TRANSACTIONS[row]), transactions.asList().get(row));
            }
            Path pdf = temporaryDirectory.resolve("hybrid.pdf");
            document.save(pdf.toFile());
            HybridParser parser = new HybridParser(pdf.toString()).dpi(144).diagnostics(true);
            assertEquals(tables.stream().map(Table::asList).toList(),
                    parser.parseResult().tables().stream().map(Table::asList).toList());
        }
    }

    @Test
    void hybridScoreIsBoundedAndDoesNotRewardEmptyExtraColumns() throws Exception {
        var score = HybridParser.class.getDeclaredMethod("score", Table.class);
        score.setAccessible(true);
        Table correct = new Table(List.of(List.of("A", "B", "C", "D", "E", "F", "G")), List.of(), List.of());
        Table split = new Table(List.of(List.of("A", "", "B", "", "C", "", "D", "", "E", "", "F", "", "G")), List.of(), List.of());
        double correctScore = (double) score.invoke(null, correct);
        assertTrue(correctScore <= 1.0);
        assertTrue(correctScore > (double) score.invoke(null, split));
    }

    private static PDDocument statement(boolean ruled, boolean summary) throws Exception {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        try (PDPageContentStream content = new PDPageContentStream(document, page)) {
            if (summary) {
                text(content, 40, 740, "SAMPLE BANK");
                text(content, 40, 720, "Customer address");
                text(content, 270, 720, "Account 123");
                if (ruled) grid(content, new float[]{40, 160, 280}, 680, 2);
                text(content, 45, 666, "Opening"); text(content, 165, 666, "1000.00");
                if (ruled) { text(content, 45, 646, "Closing"); text(content, 165, 646, "1140.00"); }
            }
            if (ruled) grid(content, COLUMNS, 600, TRANSACTIONS.length);
            for (int row = 0; row < TRANSACTIONS.length; row++) {
                for (int column = 0; column < TRANSACTIONS[row].length; column++) {
                    if (!TRANSACTIONS[row][column].isEmpty()) {
                        text(content, COLUMNS[column] + 5, 586 - row * 20, TRANSACTIONS[row][column]);
                    }
                }
            }
        }
        return document;
    }

    private static void text(PDPageContentStream content, float x, float y, String value) throws Exception {
        content.beginText();
        content.setFont(PDType1Font.HELVETICA, 8);
        content.newLineAtOffset(x, y);
        content.showText(value);
        content.endText();
    }

    private static void grid(PDPageContentStream content, float[] columns, float top, int rows) throws Exception {
        content.setLineWidth(0.5f);
        for (float x : columns) {
            content.moveTo(x, top); content.lineTo(x, top - rows * 20);
        }
        for (int row = 0; row <= rows; row++) {
            content.moveTo(columns[0], top - row * 20);
            content.lineTo(columns[columns.length - 1], top - row * 20);
        }
        content.stroke();
    }
}

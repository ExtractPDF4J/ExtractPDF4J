package com.extractpdf4j.parsers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.extractpdf4j.helpers.Table;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LatticeParserTest {

    @Test
    void configFluentSetters() throws Exception {
        LatticeParser p = new LatticeParser("f.pdf").dpi(280f).debug(true).keepCells(true).debugDir(new File("dbg"));
        Field dpi = LatticeParser.class.getDeclaredField("renderDpi");
        Field dbg = LatticeParser.class.getDeclaredField("debug");
        Field keep = LatticeParser.class.getDeclaredField("keepCells");
        Field dir = LatticeParser.class.getDeclaredField("debugDir");
        dpi.setAccessible(true); dbg.setAccessible(true); keep.setAccessible(true); dir.setAccessible(true);
        assertEquals(280f, (float)dpi.get(p), 0.001);
        assertTrue((boolean)dbg.get(p));
        assertTrue((boolean)keep.get(p));
        assertEquals(new File("dbg"), dir.get(p));
    }

    private static String scannedPdf;

@BeforeAll
static void setup() {
    // Path to your test scanned PDF
    scannedPdf = "resources/Scanned_Bank_Statement.pdf";
    File f = new File(scannedPdf);
    assertTrue(f.exists(), "Scanned test PDF must exist: " + f.getAbsolutePath());
}

@Test
void ocrExtractsTextFromCells() throws Exception {
    LatticeParser parser = new LatticeParser(scannedPdf)
            .dpi(300f)         // reasonable DPI for OCR
            .debug(true)       // optional debug output
            .keepCells(true);

    List<Table> tables = parser.pages("1").parse(); // parse first page

    assertNotNull(tables, "Tables list should not be null");
    assertFalse(tables.isEmpty(), "At least one table should be detected");

    Table table = tables.get(0);
    assertTrue(table.nrows() > 0, "Table should have at least one row");
    assertTrue(table.ncols() > 0, "Table should have at least one column");

    boolean hasNonEmptyCell = table.nrows() > 0 && table.ncols() > 0 &&
            table.cell(0, 0) != null && !table.cell(0, 0).isBlank();
    assertTrue(hasNonEmptyCell, "OCR should populate at least one cell with text");
}

@Test
void debugArtifactsDirectoryIsCreated() throws Exception {
    File debugDir = new File("target/test-debug");
    if (debugDir.exists()) {
        for (File f : debugDir.listFiles()) f.delete();
        debugDir.delete();
    }

    LatticeParser parser = new LatticeParser(scannedPdf)
            .dpi(300f)
            .debug(true)
            .keepCells(true)
            .debugDir(debugDir);

    parser.pages("1").parse();

    assertTrue(debugDir.exists(), "Debug directory should be created after parse");
}
}

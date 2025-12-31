package com.extractpdf4j.parsers;

import com.extractpdf4j.helpers.Table;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HybridParserTest {

    @Test
    void pagesPropagatesToSubparsers() throws Exception {
        HybridParser hp = new HybridParser("file.pdf");
        hp.pages("2-3");

        Field fStream = HybridParser.class.getDeclaredField("stream");
        Field fLattice = HybridParser.class.getDeclaredField("lattice");
        Field fOcr = HybridParser.class.getDeclaredField("ocrstream");
        fStream.setAccessible(true);
        fLattice.setAccessible(true);
        fOcr.setAccessible(true);

        BaseParser sp = (BaseParser) fStream.get(hp);
        BaseParser lp = (BaseParser) fLattice.get(hp);
        BaseParser op = (BaseParser) fOcr.get(hp);

        Field pages = BaseParser.class.getDeclaredField("pages");
        pages.setAccessible(true);
        assertEquals("2-3", pages.get(sp));
        assertEquals("2-3", pages.get(lp));
        assertEquals("2-3", pages.get(op));
    }

    @Test
    void scorePrefersFilledTables() throws Exception {
        Table sparse = new Table(List.of(
                new java.util.ArrayList<>(List.of("","1","")),
                new java.util.ArrayList<>(List.of("","",""))
        ), List.of(0.0, 10.0, 20.0), List.of(0.0,10.0,20.0));

        Table dense = new Table(List.of(
                new java.util.ArrayList<>(List.of("1","2")),
                new java.util.ArrayList<>(List.of("3","4"))
        ), List.of(0.0, 10.0, 20.0), List.of(0.0,10.0,20.0));

        Method score = HybridParser.class.getDeclaredMethod("score", com.extractpdf4j.helpers.Table.class);
        score.setAccessible(true);
        double sSparse = (double) score.invoke(null, sparse);
        double sDense  = (double) score.invoke(null, dense);
        assertTrue(sDense > sSparse);
    }
}
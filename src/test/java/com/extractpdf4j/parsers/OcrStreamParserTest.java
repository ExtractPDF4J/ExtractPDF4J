package com.extractpdf4j.parsers;

import com.extractpdf4j.helpers.Ocr;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OcrStreamParserTest {


    private static boolean invokeMatch(OcrStreamParser p, List<Ocr.OcrWord> line) throws Exception {
        Method m = OcrStreamParser.class.getDeclaredMethod("matchRequiredHeaders", List.class);
        m.setAccessible(true);
        return (boolean) m.invoke(p, line);
    }

    @Test
    void configFluentSetters() throws Exception {
        OcrStreamParser p = new OcrStreamParser("f.pdf").dpi(400f).debug(true).debugDir(new File("dbg"));
        Field dpi = OcrStreamParser.class.getDeclaredField("renderDpi");
        Field dbg = OcrStreamParser.class.getDeclaredField("debug");
        Field dir = OcrStreamParser.class.getDeclaredField("debugDir");
        dpi.setAccessible(true); dbg.setAccessible(true); dir.setAccessible(true);
        assertEquals(400f, (float)dpi.get(p), 0.001);
        assertTrue((boolean)dbg.get(p));
        assertEquals(new File("dbg"), dir.get(p));
    }

    @Test
    void locateClampsToLastColumn() throws Exception {
        List<Double> bounds = List.of(0.0, 10.0, 20.0);
        Method m = OcrStreamParser.class.getDeclaredMethod("locate", List.class, double.class);
        m.setAccessible(true);
        assertEquals(0, (int)m.invoke(null, bounds, 5.0));
        assertEquals(1, (int)m.invoke(null, bounds, 15.0));
        assertEquals(1, (int)m.invoke(null, bounds, 25.0)); // clamp
    }

    @Test
    void requiredHeaders_allPresent_returnsTrue() throws Exception {
        OcrStreamParser p = new OcrStreamParser("f.pdf")
                .requiredHeaders(List.of("date", "description", "balance"));

        List<Ocr.OcrWord> line = List.of(
                new Ocr.OcrWord(0,0,0,0,0,"Date",0,0,0,0),
                new Ocr.OcrWord(0,0,0,0,0,"Description",0,0,0,0),
                new Ocr.OcrWord(0,0,0,0,0,"Balance",0,0,0,0)
        );

        assertTrue(invokeMatch(p, line));
    }

    @Test
    void requiredHeaders_missingOne_returnsFalse() throws Exception {
        OcrStreamParser p = new OcrStreamParser("f.pdf")
                .requiredHeaders(List.of("date", "description", "balance"));

        List<Ocr.OcrWord> line = List.of(
                new Ocr.OcrWord(0,0,0,0,0,"Date",0,0,0,0),
                new Ocr.OcrWord(0,0,0,0,0,"Description",0,0,0,0)
        );

        assertFalse(invokeMatch(p, line));
    }

    @Test
    void requiredHeaders_smallOcrTypos_returnsTrue() throws Exception {
        OcrStreamParser p = new OcrStreamParser("f.pdf")
                .requiredHeaders(List.of("date", "description", "balance"));

        List<Ocr.OcrWord> line = List.of(
                new Ocr.OcrWord(0,0,0,0,0,"Datc",0,0,0,0),
                new Ocr.OcrWord(0,0,0,0,0,"Descriptlon",0,0,0,0),
                new Ocr.OcrWord(0,0,0,0,0,"Balance",0,0,0,0)
        );

        assertTrue(invokeMatch(p, line));
    }
}

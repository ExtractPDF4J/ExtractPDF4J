package com.extractpdf4j.parsers;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OcrStreamParserTest {

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
}

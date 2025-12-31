package com.extractpdf4j.parsers;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Tests algorithmic helpers inside StreamParser via reflection (no PDF). */
class StreamParserTest {

    @Test
    void findColAndInferBounds() throws Exception {
        Class<?> glyphCls = Class.forName("com.extractpdf4j.parsers.StreamParser$Glyph");
        Class<?> rowCls   = Class.forName("com.extractpdf4j.parsers.StreamParser$Row");

        Constructor<?> gCtor = glyphCls.getDeclaredConstructors()[0];
        gCtor.setAccessible(true);

        List<Object> glyphs1 = new ArrayList<>();
        glyphs1.add(gCtor.newInstance("A",  5.0, 10.0, 5.0));
        glyphs1.add(gCtor.newInstance("B", 12.0, 10.0, 5.0));
        glyphs1.add(gCtor.newInstance("C", 35.0, 10.0, 5.0));
        glyphs1.add(gCtor.newInstance("D", 42.0, 10.0, 5.0));

        List<Object> glyphs2 = new ArrayList<>();
        glyphs2.add(gCtor.newInstance("E",  6.0, 20.0, 5.0));
        glyphs2.add(gCtor.newInstance("F", 14.0, 20.0, 5.0));
        glyphs2.add(gCtor.newInstance("G", 37.0, 20.0, 5.0));
        glyphs2.add(gCtor.newInstance("H", 46.0, 20.0, 5.0));

        var rows = new ArrayList<>();
        var rCtor = rowCls.getDeclaredConstructor(List.class);
        rCtor.setAccessible(true);
        rows.add(rCtor.newInstance(glyphs1));
        rows.add(rCtor.newInstance(glyphs2));

        Method infer = StreamParser.class.getDeclaredMethod("inferColumnBounds", List.class);
        infer.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Double> bounds = (List<Double>) infer.invoke(null, rows);

        assertTrue(bounds.size() >= 3); // includes margins
        Method findCol = StreamParser.class.getDeclaredMethod("findCol", List.class, double.class);
        findCol.setAccessible(true);
        int c0 = (int) findCol.invoke(null, bounds, 7.0);
        int c1 = (int) findCol.invoke(null, bounds, 36.0);
        assertTrue(c1 > c0);
    }
}

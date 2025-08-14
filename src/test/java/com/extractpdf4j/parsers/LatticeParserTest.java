package com.extractpdf4j.parsers;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;

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
}

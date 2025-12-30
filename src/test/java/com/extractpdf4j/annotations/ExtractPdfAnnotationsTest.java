package com.extractpdf4j.annotations;

import com.extractpdf4j.parsers.BaseParser;
import com.extractpdf4j.parsers.LatticeParser;
import com.extractpdf4j.parsers.OcrStreamParser;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtractPdfAnnotationsTest {
    @ExtractPdfConfig(
            parser = ParserMode.LATTICE,
            pages = "2-4",
            stripText = false,
            dpi = 300f,
            debug = true,
            keepCells = true,
            debugDir = "build/debug"
    )
    private static class LatticeConfig {
    }

    @ExtractPdfConfig(
            parser = ParserMode.OCRSTREAM,
            dpi = 350f,
            debugDir = "build/ocr",
            requiredHeaders = {"Date", "Amount"}
    )
    private static class OcrConfig {
    }

    @Test
    void buildsLatticeParserFromAnnotation() throws Exception {
        BaseParser parser = ExtractPdfAnnotations.parserFrom(LatticeConfig.class, "sample.pdf");
        assertInstanceOf(LatticeParser.class, parser);

        assertEquals("2-4", getBaseField(parser, "pages"));
        assertEquals(false, getBaseField(parser, "stripText"));

        LatticeParser lattice = (LatticeParser) parser;
        assertEquals(300f, getField(lattice, "renderDpi"));
        assertEquals(true, getField(lattice, "debug"));
        assertEquals(true, getField(lattice, "keepCells"));
        assertEquals(new File("build/debug"), getField(lattice, "debugDir"));
    }

    @Test
    void appliesOcrHeadersAndDpi() throws Exception {
        BaseParser parser = ExtractPdfAnnotations.parserFrom(OcrConfig.class, "sample.pdf");
        assertInstanceOf(OcrStreamParser.class, parser);

        OcrStreamParser ocr = (OcrStreamParser) parser;
        assertEquals(350f, getField(ocr, "renderDpi"));
        assertEquals(new File("build/ocr"), getField(ocr, "debugDir"));

        @SuppressWarnings("unchecked")
        List<String> headers = (List<String>) getField(ocr, "requiredHeaders");
        assertEquals(List.of("Date", "Amount"), headers);
        assertTrue((Boolean) getField(ocr, "debug") == false);
    }

    private static Object getBaseField(BaseParser parser, String name) throws Exception {
        Field field = BaseParser.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(parser);
    }

    private static Object getField(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }
}
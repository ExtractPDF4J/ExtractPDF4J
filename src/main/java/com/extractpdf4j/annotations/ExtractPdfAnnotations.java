package com.extractpdf4j.annotations;

import com.extractpdf4j.parsers.BaseParser;
import com.extractpdf4j.parsers.HybridParser;
import com.extractpdf4j.parsers.LatticeParser;
import com.extractpdf4j.parsers.OcrStreamParser;
import com.extractpdf4j.parsers.StreamParser;
import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Factory methods for creating configured parsers from {@link ExtractPdfConfig} annotations.
 */
public final class ExtractPdfAnnotations {
    private ExtractPdfAnnotations() {
    }

    /**
     * Builds a parser instance from the {@link ExtractPdfConfig} annotation on a class.
     *
     * @param type annotated class
     * @param filepath optional PDF file path (null for in-memory usage)
     * @return configured parser instance
     * @throws IllegalArgumentException if no annotation is present
     */
    public static BaseParser parserFrom(Class<?> type, String filepath) {
        ExtractPdfConfig config = requireConfig(type);
        BaseParser parser = createParser(config, filepath);
        applyCommonConfig(parser, config);
        applyParserSpecificConfig(parser, config);
        return parser;
    }

    /**
     * Builds a parser instance (no filepath) from the {@link ExtractPdfConfig} annotation on a class.
     *
     * @param type annotated class
     * @return configured parser instance
     * @throws IllegalArgumentException if no annotation is present
     */
    public static BaseParser parserFrom(Class<?> type) {
        return parserFrom(type, null);
    }

    private static ExtractPdfConfig requireConfig(Class<?> type) {
        ExtractPdfConfig config = type.getAnnotation(ExtractPdfConfig.class);
        if (config == null) {
            throw new IllegalArgumentException("Missing @ExtractPdfConfig on " + type.getName());
        }
        return config;
    }

    private static BaseParser createParser(ExtractPdfConfig config, String filepath) {
        return switch (config.parser()) {
            case STREAM -> (filepath == null) ? new StreamParser() : new StreamParser(filepath);
            case LATTICE -> (filepath == null) ? new LatticeParser() : new LatticeParser(filepath);
            case OCRSTREAM -> (filepath == null) ? new OcrStreamParser() : new OcrStreamParser(filepath);
            case HYBRID -> (filepath == null) ? new HybridParser() : new HybridParser(filepath);
        };
    }

    private static void applyCommonConfig(BaseParser parser, ExtractPdfConfig config) {
        if (config.pages() != null && !config.pages().isBlank()) {
            parser.pages(config.pages());
        }
        parser.stripText(config.stripText());
    }

    private static void applyParserSpecificConfig(BaseParser parser, ExtractPdfConfig config) {
        File debugDir = config.debugDir().isBlank() ? null : new File(config.debugDir());
        List<String> requiredHeaders = Arrays.asList(config.requiredHeaders());

        if (parser instanceof StreamParser) {
            return;
        }

        if (parser instanceof LatticeParser lattice) {
            lattice.dpi(config.dpi());
            lattice.debug(config.debug());
            lattice.keepCells(config.keepCells());
            lattice.debugDir(debugDir);
            return;
        }

        if (parser instanceof OcrStreamParser ocrStream) {
            ocrStream.dpi(config.dpi());
            ocrStream.debug(config.debug());
            ocrStream.debugDir(debugDir);
            if (!requiredHeaders.isEmpty() && !(requiredHeaders.size() == 1 && requiredHeaders.get(0).isBlank())) {
                ocrStream.requiredHeaders(requiredHeaders);
            }
            return;
        }

        if (parser instanceof HybridParser hybrid) {
            hybrid.dpi(config.dpi());
            hybrid.debug(config.debug());
            hybrid.keepCells(config.keepCells());
            hybrid.debugDir(debugDir);
            hybrid.minScore(config.minScore());
        }
    }
}
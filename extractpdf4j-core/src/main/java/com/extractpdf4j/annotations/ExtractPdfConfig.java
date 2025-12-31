package com.extractpdf4j.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation-based configuration for ExtractPDF4J parsers.
 *
 * <p>Apply this annotation to a class to declare parser settings that can be
 * materialized via {@link ExtractPdfAnnotations}.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExtractPdfConfig {
    /**
     * Parser strategy to use when materializing a parser.
     */
    ParserMode parser() default ParserMode.HYBRID;

    /**
     * Page selection string (e.g., "all", "1", "2-5", "1,3-4").
     */
    String pages() default "1";

    /**
     * Whether to strip/normalize text for stream-based extraction.
     */
    boolean stripText() default true;

    /**
     * DPI for image-based parsing (lattice/ocr/hybrid).
     */
    float dpi() default 450f;

    /**
     * Enables debug artifact output for lattice/ocr/hybrid.
     */
    boolean debug() default false;

    /**
     * Whether to keep empty cells in lattice parsing.
     */
    boolean keepCells() default false;

    /**
     * Minimum average score for hybrid parser selection.
     */
    double minScore() default 0.0;

    /**
     * Directory where debug artifacts should be written.
     */
    String debugDir() default "";

    /**
     * Required OCR headers to look for before returning results.
     */
    String[] requiredHeaders() default {};
}
package com.extractpdf4j.ml;

import com.extractpdf4j.helpers.Table;
import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * Optional machine-learning table extraction strategy used by hybrid parsing.
 *
 * <p>The core module owns only this lightweight contract. Implementations may
 * live in an optional module and use ONNX Runtime or another local inference
 * engine without adding ML dependencies to {@code extractpdf4j-core}.</p>
 */
public interface MlTableExtractor {

    /**
     * Extracts tables from a PDF file.
     *
     * @param filepath PDF path
     * @param pages page selection using ExtractPDF4J syntax
     * @return extracted tables, never {@code null}
     * @throws IOException when the document or model cannot be processed
     */
    List<Table> extract(String filepath, String pages) throws IOException;

    /**
     * Extracts tables from an in-memory PDF document.
     *
     * @param document open PDFBox document owned by the caller
     * @param pages page selection using ExtractPDF4J syntax
     * @return extracted tables, never {@code null}
     * @throws IOException when the document or model cannot be processed
     */
    List<Table> extract(PDDocument document, String pages) throws IOException;
}

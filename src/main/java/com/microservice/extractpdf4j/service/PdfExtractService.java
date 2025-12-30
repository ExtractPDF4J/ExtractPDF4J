
package com.microservice.extractpdf4j.service;

import com.extractpdf4j.annotations.ExtractPdfAnnotations;
import com.extractpdf4j.annotations.ExtractPdfConfig;
import com.extractpdf4j.annotations.ParserMode;
import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.BaseParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author ronaldopaulino
 */



@Service
public class PdfExtractService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PdfExtractService.class);

    @ExtractPdfConfig(
            parser = ParserMode.HYBRID,
            pages = "all"
    )
    private static class DefaultParserConfig {
    }

    /**
     * Asynchronously extracts tables from a given PDF file.
     * This method is executed on a background thread pool managed by Spring.
     *
     * @param file The PDF file uploaded by the user.
     * @return A CompletableFuture containing the extracted tables formatted as a single CSV string.
     *         The future will complete exceptionally if an error occurs during processing.
     */
    @Async
    public CompletableFuture<String> extractTablesAsCsv(MultipartFile file) {
        LOGGER.info("Starting ASYNC PDF parsing for file: {}", file.getOriginalFilename());
        
        try {
            String result = processFile(file);
            // Wrap the successful result in a completed CompletableFuture
            return CompletableFuture.completedFuture(result);
        } catch (IOException e) {
            LOGGER.error("An I/O error occurred during ASYNC PDF processing.", e);
            // Wrap the exception in a failed CompletableFuture
            return CompletableFuture.failedFuture(e);
        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred during ASYNC PDF processing.", e);
            // Catch any other unexpected errors
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * The core synchronous processing logic for a single PDF file.
     *
     * @param file The PDF file to process.
     * @return The formatted string of extracted tables.
     * @throws IOException if the file is empty or a PDFBox error occurs.
     */
    private String processFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("The provided file is null or empty.");
        }

        // Use a try-with-resources statement to ensure the PDDocument is closed automatically
        try (PDDocument document = PDDocument.load(file.getInputStream())) {

            BaseParser parser = ExtractPdfAnnotations.parserFrom(DefaultParserConfig.class);

            // 2. Call the parse method that accepts a PDDocument
            List<Table> tables = parser.parse(document);

            LOGGER.info("Found {} tables in the document.", tables.size());

            // 3. Format the results into a single CSV string
            if (tables.isEmpty()) {
                return "No tables were found in the document.";
            }

            return formatTablesToCsv(tables);
        }
    }

    /**
     * Formats a list of Table objects into a single, human-readable string.
     *
     * @param tables The list of tables to format.
     * @return A string with each table represented in CSV format, separated by headers.
     */
    private String formatTablesToCsv(List<Table> tables) {
        StringBuilder csvOutput = new StringBuilder();
        
        for (int i = 0; i < tables.size(); i++) {
            csvOutput.append("--- Table ").append(i + 1).append(" ---\n");
            csvOutput.append(tables.get(i).toCSV(','));
            csvOutput.append("\n\n");
        }
        
        return csvOutput.toString().trim();
    }
}
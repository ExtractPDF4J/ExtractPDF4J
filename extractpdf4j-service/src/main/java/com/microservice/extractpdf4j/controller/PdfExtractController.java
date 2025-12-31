package com.microservice.extractpdf4j.controller;

import com.microservice.extractpdf4j.service.PdfExtractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/extract")
public class PdfExtractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PdfExtractController.class);
    private final PdfExtractService pdfExtractService;

    public PdfExtractController(PdfExtractService pdfExtractService) {
        this.pdfExtractService = pdfExtractService;
    }

     @PostMapping(
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.TEXT_PLAIN_VALUE 
    )
    public ResponseEntity<String> extractPdf(@RequestParam("file") MultipartFile file) {
        
        LOGGER.info("Received a file upload request: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload.");
        }

        try {
            // 1. Call the async method. This returns immediately with a CompletableFuture.
            CompletableFuture<String> futureResult = pdfExtractService.extractTablesAsCsv(file);

            // 2. Wait for the future to complete and get the result.
            //    The .join() method will block here until the async task is done.
            String csvResult = futureResult.join();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);

            return new ResponseEntity<>(csvResult, headers, HttpStatus.OK);

        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred in the controller or async task.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An internal error occurred: " + e.getMessage());
        }
    }
}
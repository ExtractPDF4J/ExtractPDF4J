
package com.microservice.extractpdf4j.controller;

import com.microservice.extractpdf4j.service.PdfExtractService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import java.util.concurrent.CompletableFuture;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Unit tests for the {@link PdfExtractController}.
 * Uses {@link WebMvcTest} to test the web layer without a full application context.
 */
@WebMvcTest(PdfExtractController.class)
class PdfExtractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Use @MockitoBean, the modern replacement for the deprecated @MockBean
    @MockitoBean
    private PdfExtractService pdfExtractService;

    @Test
    void extractPdf_shouldReturnOkAndCsvContent_whenFileIsValid() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "fake-pdf-content".getBytes()
        );

        String expectedCsvResponse = "--- Table 1 ---\ncell1,cell2\n\n";

        // Configure the mocked service to return a completed future with the expected response
        when(pdfExtractService.extractTablesAsCsv(any(MockMultipartFile.class)))
                .thenReturn(CompletableFuture.completedFuture(expectedCsvResponse));

        // Act & Assert
        // Perform a multipart POST request and verify the response
        mockMvc.perform(multipart("/api/extract").file(file))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string(expectedCsvResponse));
    }

    @Test
    void extractPdf_shouldReturnBadRequest_whenFileIsEmpty() throws Exception {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                new byte[0]
        );

        // Act & Assert
        // Perform the request with the empty file and expect a 400 Bad Request status
        mockMvc.perform(multipart("/api/extract").file(emptyFile))
                .andExpect(status().isBadRequest());
    }
}
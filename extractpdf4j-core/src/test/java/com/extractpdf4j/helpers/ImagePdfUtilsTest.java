package com.extractpdf4j.helpers;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/** Uses real PDFBox (no mocking) by rendering an empty page. */
class ImagePdfUtilsTest {

    @Test
    void renderPageCreatesAnImage() throws Exception {
        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            BufferedImage img = ImagePdfUtils.renderPage(doc, 0, 72f);
            assertNotNull(img);
            assertTrue(img.getWidth() > 0 && img.getHeight() > 0);
        }
    }
}

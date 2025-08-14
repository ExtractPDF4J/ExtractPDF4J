package com.extractpdf4j.helpers;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** No mocking: check benign behavior paths regardless of local Tesseract availability. */
class OcrTest {

    @Test
    void bytedecoModeReturnsEmptyOrText() {
        System.setProperty("ocr", "bytedeco");
        try {
            String s = Ocr.ocrPng("does_not_matter.png");
            assertNotNull(s);
        } finally {
            System.clearProperty("ocr");
        }
    }

    @Test
    void ocrTsvReturnsListEvenIfTesseractMissing() throws Exception {
        File tmp = File.createTempFile("blank_", ".png");
        try {
            BufferedImage bi = new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_GRAY);
            ImageIO.write(bi, "png", tmp);
            List<Ocr.OcrWord> words = Ocr.ocrTsv(tmp.getAbsolutePath());
            assertNotNull(words);
        } finally {
            tmp.delete();
        }
    }
}

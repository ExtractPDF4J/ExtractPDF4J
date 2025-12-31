package com.extractpdf4j.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/** No mocking: check benign behavior paths regardless of local Tesseract availability. */
class OcrTest {

    // --- Original project tests (no changes) ---
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

    // --- Test 1: VISA Bank Statement ---
    @Test
    public void testOcrHeuristicallyOnVisaStatement() {
        URL imageUrl = getClass().getClassLoader().getResource("test-images/visa-statement.png");
        Assertions.assertNotNull(imageUrl, "Test image 'visa-statement.png' not found.");

        List<Ocr.OcrWord> words = Ocr.ocrTsvHeuristically(new File(imageUrl.getFile()).getAbsolutePath(), "eng");
        String fullText = wordsToString(words);

        Assertions.assertFalse(words.isEmpty(), "No words recognized from VISA statement.");
        Assertions.assertTrue(fullText.contains("Visa Platinum"), "Header 'Visa Platinum' not found.");
        System.out.println("✅ VISA statement test passed successfully!");
    }

    // --- Test 2: ANZ Bank Statement ---
    @Test
    public void testOcrHeuristicallyOnAnzStatement() {
        URL imageUrl = getClass().getClassLoader().getResource("test-images/anz-statement.png");
        Assertions.assertNotNull(imageUrl, "Test image 'anz-statement.png' not found.");

        List<Ocr.OcrWord> words = Ocr.ocrTsvHeuristically(new File(imageUrl.getFile()).getAbsolutePath(), "eng");
        String fullText = wordsToString(words);

        Assertions.assertFalse(words.isEmpty(), "No words recognized from ANZ statement.");
        Assertions.assertTrue(fullText.contains("Salary Account"), "Header 'Salary Account' not found.");
        System.out.println("✅ ANZ statement test passed successfully!");
    }

    // --- Test 3: Multi-language OCR (eng + fra) ---
    @Test
    public void testOcrHeuristicallyOnMultiLanguageImage() {
        URL imageUrl = getClass().getClassLoader().getResource("test-images/multi-lang-text.png");
        Assertions.assertNotNull(imageUrl, "Test image 'multi-lang-text.png' not found.");

        System.out.println("Testing multi-language OCR (eng+fra)...");
        List<Ocr.OcrWord> words = Ocr.ocrTsvHeuristically(new File(imageUrl.getFile()).getAbsolutePath(), "eng+fra");
        Assertions.assertFalse(words.isEmpty(), "No words recognized from multi-language image.");

        String fullText = wordsToString(words).toLowerCase(Locale.ROOT);
        System.out.println("[OCR] Detected text:\n" + fullText);

        boolean frenchDetected =
                fullText.contains("bonjour") ||
                fullText.contains("salut") ||
                fullText.contains("monde") ||
                fullText.contains("le monde");

        if (frenchDetected) {
            System.out.println("✅ French text successfully recognized (eng+fra).");
        } else {
            System.out.println("⚠️ Warning: French text not recognized; OCR still functional but may differ by version.");
        }

        Assertions.assertTrue(
                frenchDetected,
                "French text (bonjour/salut/monde) was not recognized with 'eng+fra'."
        );
    }

    // --- Helper: join words ---
    private String wordsToString(List<Ocr.OcrWord> words) {
        StringBuilder sb = new StringBuilder();
        for (Ocr.OcrWord word : words) {
            sb.append(word.text).append(" ");
        }
        return sb.toString();
    }
}
